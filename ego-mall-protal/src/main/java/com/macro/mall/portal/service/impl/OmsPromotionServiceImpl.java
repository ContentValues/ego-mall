package com.macro.mall.portal.service.impl;

import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.PmsProductFullReduction;
import com.macro.mall.model.PmsProductLadder;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.portal.dao.PortalProductDao;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.PromotionProduct;
import com.macro.mall.portal.service.OmsPromotionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-07-13 13:18
 **/
@Service
public class OmsPromotionServiceImpl implements OmsPromotionService {

    @Resource
    PortalProductDao portalProductDao;

    @Override
    public List<CartPromotionItem> calcCartPromotion(List<OmsCartItem> cartItemList) {

        //1.先根据productId对CartItem进行分组，以spu为单位进行计算优惠
        Map<Long, List<OmsCartItem>> productCartMap = groupCartItemBySpu(cartItemList);
        //2.查询所有商品的优惠相关信息
        List<PromotionProduct> promotionProductList = getPromotionProductList(cartItemList);

        //3.根据商品促销类型计算商品促销优惠价格
        List<CartPromotionItem> cartPromotionItemList = new ArrayList<>();

        for (Map.Entry<Long, List<OmsCartItem>> entry : productCartMap.entrySet()) {
            Long productId = entry.getKey();
            PromotionProduct promotionProduct = getPromotionProductById(productId, promotionProductList);
            List<OmsCartItem> itemList = entry.getValue();
            //促销类型：0->没有促销使用原价;1->使用促销价；2->使用会员价；3->使用阶梯价格；4->使用满减价格；5->限时购
            Integer promotionType = promotionProduct.getPromotionType();
            if (promotionType == 1) {
                for (OmsCartItem omsCartItem : itemList) {
                    PmsSkuStock skuStock = getPmsSkuStockById(omsCartItem.getProductSkuId(), promotionProduct.getSkuStockList());
                    CartPromotionItem cartPromotionItem = new CartPromotionItem();
                    BeanUtils.copyProperties(omsCartItem, cartPromotionItem);
                    cartPromotionItem.setPromotionMessage("使用促销价");
                    cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
                    cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
                    cartPromotionItem.setRealStock(skuStock.getStock() - skuStock.getLockStock());
                    cartPromotionItem.setReduceAmount(skuStock.getPrice().subtract(skuStock.getPromotionPrice()));
                    cartPromotionItemList.add(cartPromotionItem);
                }
            } else if (promotionType == 3) {

                int count = getCartItemCount(itemList);
                PmsProductLadder ladder = getProductLadder(count, promotionProduct.getProductLadderList());
                if (ladder != null) {
                    for (OmsCartItem omsCartItem : itemList) {
                        CartPromotionItem cartPromotionItem = new CartPromotionItem();
                        BeanUtils.copyProperties(omsCartItem, cartPromotionItem);
                        cartPromotionItem.setPromotionMessage(getLadderPromotionMessage(ladder));
                        cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
                        cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
                        PmsSkuStock skuStock = getPmsSkuStockById(omsCartItem.getProductSkuId(), promotionProduct.getSkuStockList());
                        cartPromotionItem.setRealStock(skuStock.getStock() - skuStock.getLockStock());
                        //(商品原价-折扣*商品原价)
                        BigDecimal originalPrice = skuStock.getPrice();
                        BigDecimal reduceAmount = (originalPrice.subtract(ladder.getDiscount().multiply(originalPrice)));
//                                .multiply(new BigDecimal(omsCartItem.getQuantity()));
                        cartPromotionItem.setReduceAmount(reduceAmount);
                        cartPromotionItemList.add(cartPromotionItem);
                    }

                } else {
                    handleNoReduce(cartPromotionItemList, itemList, promotionProduct);
                }


            } else if (promotionType == 4) {
                //购物车总价
                BigDecimal totalAmount = getCartItemAmount(itemList, promotionProductList);
                PmsProductFullReduction fullReduction = getProductFullReduction(totalAmount, promotionProduct.getProductFullReductionList());
                if (fullReduction != null) {
                    /**
                     * (商品原价/总价)*满减金额
                     * 满减的价格平摊到每一个同类商品的条目
                     * 例如  小米A  库存是A1
                     *      小米A  库存是A2
                     */
                    for (OmsCartItem omsCartItem : itemList) {
                        CartPromotionItem cartPromotionItem = new CartPromotionItem();
                        BeanUtils.copyProperties(omsCartItem, cartPromotionItem);
                        cartPromotionItem.setPromotionMessage(getFullReductionPromotionMessage(fullReduction));
                        cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
                        cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
                        PmsSkuStock skuStock = getPmsSkuStockById(omsCartItem.getProductSkuId(), promotionProduct.getSkuStockList());
                        cartPromotionItem.setRealStock(skuStock.getStock() - skuStock.getLockStock());
                        cartPromotionItem.setReduceAmount(
                                skuStock.getPrice()
//                                        .multiply(new BigDecimal(cartPromotionItem.getQuantity()))
                                        .divide(totalAmount, RoundingMode.HALF_EVEN)
                                        .multiply(fullReduction.getReducePrice())
                        );
                        cartPromotionItemList.add(cartPromotionItem);
                    }
                } else {
                    handleNoReduce(cartPromotionItemList, itemList, promotionProduct);
                }
            } else {
                handleNoReduce(cartPromotionItemList, itemList, promotionProduct);
            }
        }
        return cartPromotionItemList;
    }


    /**
     * 获取打折优惠的促销信息
     */
    private String getLadderPromotionMessage(PmsProductLadder ladder) {
        StringBuilder sb = new StringBuilder();
        sb.append("打折优惠：");
        sb.append("满");
        sb.append(ladder.getCount());
        sb.append("件，");
        sb.append("打");
        sb.append(ladder.getDiscount().multiply(new BigDecimal(10)));
        sb.append("折");
        return sb.toString();
    }


    /**
     * 对没满足优惠条件的商品进行处理
     */
    private void handleNoReduce(List<CartPromotionItem> cartPromotionItemList, List<OmsCartItem> itemList, PromotionProduct promotionProduct) {
        for (OmsCartItem item : itemList) {
            CartPromotionItem cartPromotionItem = new CartPromotionItem();
            BeanUtils.copyProperties(item, cartPromotionItem);
            cartPromotionItem.setPromotionMessage("无优惠");
            cartPromotionItem.setReduceAmount(new BigDecimal(0));
            PmsSkuStock skuStock = getPmsSkuStockById(item.getProductSkuId(), promotionProduct.getSkuStockList());
            if (skuStock != null) {
                cartPromotionItem.setRealStock(skuStock.getStock() - skuStock.getLockStock());
            }
            cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
            cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
            cartPromotionItemList.add(cartPromotionItem);
        }
    }


    /**
     * 获取满减促销消息
     */
    private String getFullReductionPromotionMessage(PmsProductFullReduction fullReduction) {
        StringBuilder sb = new StringBuilder();
        sb.append("满减优惠：");
        sb.append("满");
        sb.append(fullReduction.getFullPrice());
        sb.append("元，");
        sb.append("减");
        sb.append(fullReduction.getReducePrice());
        sb.append("元");
        return sb.toString();
    }

    /**
     * 得到一类商品的满减信息
     *
     * @param totalAmount
     * @param fullReductionList
     * @return
     */
    private PmsProductFullReduction getProductFullReduction(BigDecimal totalAmount, List<PmsProductFullReduction> fullReductionList) {
        //按条件从高到低排序
        fullReductionList.sort(new Comparator<PmsProductFullReduction>() {
            @Override
            public int compare(PmsProductFullReduction o1, PmsProductFullReduction o2) {
                return o2.getFullPrice().subtract(o1.getFullPrice()).intValue();
            }
        });
        for (PmsProductFullReduction fullReduction : fullReductionList) {
            if (totalAmount.subtract(fullReduction.getFullPrice()).intValue() >= 0) {
                return fullReduction;
            }
        }
        return null;
    }


    /**
     * 获取购物车中指定商品的数量
     */
    private int getCartItemCount(List<OmsCartItem> itemList) {
        int count = 0;
        for (OmsCartItem item : itemList) {
            count += item.getQuantity();
        }
        return count;
    }

    /**
     * 根据购买商品数量获取满足条件的打折优惠策略
     */
    private PmsProductLadder getProductLadder(int count, List<PmsProductLadder> productLadderList) {
        //按数量从大到小排序
        productLadderList.sort(new Comparator<PmsProductLadder>() {
            @Override
            public int compare(PmsProductLadder o1, PmsProductLadder o2) {
                return o2.getCount() - o1.getCount();
            }
        });
        for (PmsProductLadder productLadder : productLadderList) {
            if (count >= productLadder.getCount()) {
                return productLadder;
            }
        }
        return null;
    }

    /**
     * 以spu为单位对购物车中商品进行分组
     */
    private Map<Long, List<OmsCartItem>> groupCartItemBySpu(List<OmsCartItem> cartItemList) {
        Map<Long, List<OmsCartItem>> productCartMap = new TreeMap<>();
        for (OmsCartItem cartItem : cartItemList) {
            List<OmsCartItem> productCartItemList = productCartMap.get(cartItem.getProductId());
            if (productCartItemList == null) {
                productCartItemList = new ArrayList<>();
                productCartItemList.add(cartItem);
                productCartMap.put(cartItem.getProductId(), productCartItemList);
            } else {
                productCartItemList.add(cartItem);
            }
        }
        return productCartMap;
    }


    /**
     * 获取购物车中指定商品的总价
     */
    private BigDecimal getCartItemAmount(List<OmsCartItem> itemList, List<PromotionProduct> promotionProductList) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OmsCartItem omsCartItem : itemList) {
            PromotionProduct promotionProduct = getPromotionProductById(omsCartItem.getProductId(), promotionProductList);
            PmsSkuStock skuStock = getPmsSkuStockById(omsCartItem.getProductSkuId(), promotionProduct.getSkuStockList());
            //单价*购买数量
            totalAmount = totalAmount.add(skuStock.getPrice().multiply(new BigDecimal(omsCartItem.getQuantity())));
        }
        return totalAmount;
    }


    /**
     * 根据库存ID获取库存信息
     *
     * @param productSkuId
     * @param skuStockList
     * @return
     */
    private PmsSkuStock getPmsSkuStockById(Long productSkuId, List<PmsSkuStock> skuStockList) {
        for (PmsSkuStock skuStock : skuStockList) {
            if (productSkuId.equals(skuStock.getId())) {
                return skuStock;
            }
        }
        return null;
    }


    /**
     * 根据商品id获取商品的促销信息
     */
    private PromotionProduct getPromotionProductById(Long productId, List<PromotionProduct> promotionProductList) {
        for (PromotionProduct promotionProduct : promotionProductList) {
            if (productId.equals(promotionProduct.getId())) {
                return promotionProduct;
            }
        }
        return null;
    }


    /**
     * 查询所有商品的优惠相关信息
     */
    private List<PromotionProduct> getPromotionProductList(List<OmsCartItem> cartItemList) {
        List<Long> productIdList = new ArrayList<>();
        for (OmsCartItem cartItem : cartItemList) {
            productIdList.add(cartItem.getProductId());
        }
        return portalProductDao.getPromotionProductList(productIdList);
    }
}