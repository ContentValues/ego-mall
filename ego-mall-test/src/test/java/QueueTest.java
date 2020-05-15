import com.macro.mall.mq.Sender;
import com.macro.mall.mq.SpringbootServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-05-13 10:21
 **/

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootServerApplication.class)
public class QueueTest {

    @Autowired
    Sender sender;

    @Test
    public void testSender() {
        while (true) {
            try {
                sender.send("Hello RabbitMQ");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testSenderFanout() {
        while (true) {
            try {
                sender.sendFanout("testSenderFanout");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}