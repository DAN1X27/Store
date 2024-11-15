package danix.app.Store.services;

import danix.app.Store.models.Item;
import danix.app.Store.models.Order;
import danix.app.Store.models.Person;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class OrdersStatusUpdater {
    private final static RestTemplate template = new RestTemplate();

    public static void main(String[] args) throws InterruptedException{
        String loginUrl = "http://localhost:8080/auth/login";
        String checkOrdersUrl = "http://localhost:8080/orders/getAll";

        Map<String, String> loginJsonData = new HashMap<>();
        loginJsonData.put("email", "orders_updater@gmail.com");
        loginJsonData.put("password", "test_password");

        HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginJsonData);

        while (true) {
            Map<String, String> response = template.postForObject(loginUrl, loginEntity, Map.class);
            String token = response.get("jwt-token");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> request = new HttpEntity<>(headers);


            ResponseEntity<String> response1 = template.exchange(
                checkOrdersUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                });

            Thread.sleep(1800000);
        }

    }
}
