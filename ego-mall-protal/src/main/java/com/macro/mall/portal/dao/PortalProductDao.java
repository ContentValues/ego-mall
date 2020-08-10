package com.macro.mall.portal.dao;

import com.macro.mall.portal.domain.CartProduct;
import com.macro.mall.portal.domain.PromotionProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 前台系统自定义商品Dao
 * Created by macro on 2018/8/2.
 */
public interface PortalProductDao {

    /**
     * 查询购物车中选择规格的商品信息
     * @param id
     * @return
     */
    CartProduct getCartProduct(@Param("id") Long id);

    /**
     * 查询所有商品的优惠相关信息
     * @param ids
     * @return
     */
    List<PromotionProduct> getPromotionProductList(@Param("ids") List<Long> ids);
}
