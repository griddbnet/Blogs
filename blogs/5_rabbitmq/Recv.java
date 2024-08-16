import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Properties;
import com.toshiba.mwcloud.gs.*;
import java.util.Date;

public class Recv {

    static public class AirData {
        @JsonProperty("ts")
        @RowKey Date ts;
        @JsonProperty("pm1")
        double pm1;
        @JsonProperty("pm25")
        double pm25;
        @JsonProperty("pm10")
        double pm10;
        @JsonProperty("pm1e")
        double pm1e;
        @JsonProperty("pm25e")
        double pm25e;
        @JsonProperty("pm10e")
        double pm10e;
        @JsonProperty("particles03")
        double particles03;
        @JsonProperty("particles05")
        double particles05;
        @JsonProperty("particles10")
        double particles10;
        @JsonProperty("particles25")
        double particles25;
        @JsonProperty("particles50")
        double particles50;
        @JsonProperty("particles100")
        double particles100;
    }

    private final static String QUEUE_NAME = "airQuality";
    private final static boolean AUTO_ACK = false;

    public static GridStore GridDBNoSQL() throws GSException {

        GridStore store = null;

        try {
            Properties props = new Properties();
            props.setProperty("notificationMember", "127.0.0.1:10001");
            props.setProperty("clusterName", "myCluster");
            props.setProperty("user", "admin");
            props.setProperty("password", "admin");
            store = GridStoreFactory.getInstance().getGridStore(props);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return store;
    }

    public static void main(String[] argv) throws Exception {
        GridStore store = null;

        try {
            store = GridDBNoSQL();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TimeSeries<AirData> container = store.getTimeSeries("aqdata", AirData.class);
        System.out.println("Connected to GridDB!");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        ObjectMapper mapper = new ObjectMapper();
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            byte[] data = delivery.getBody();
   
            try {
                AirData ad = mapper.readValue(data, AirData.class);
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ad);
                System.out.println(jsonString);
                container.put(ad);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (Exception e) {
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                System.out.println("Setting nack");
            }
        };
        channel.basicConsume(QUEUE_NAME, AUTO_ACK, deliverCallback, consumerTag -> { });
    }
}
