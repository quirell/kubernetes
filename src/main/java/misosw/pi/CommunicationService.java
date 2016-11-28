package misosw.pi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.MathContext;

@Service
public class CommunicationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    public PartialResult getPartialSum(String slave, int start, int iterations, int precision) {
        StopWatch watch = new StopWatch();
        watch.start();
        ResponseEntity<String> response = restTemplate.getForEntity(slave + "/compute/{start}/{iterations}/{precision}", String.class, start, iterations, precision);
        BigDecimal computed = new BigDecimal(response.getBody(), new MathContext(precision));
        watch.stop();
        logger.info("Processed Partial Sum by {} start {} iters {} precision {} success {}", slave, start, iterations, precision, response.getStatusCode() == HttpStatus.OK);
        if (response.getStatusCode() == HttpStatus.OK)
            return new PartialResult(computed, watch.getTotalTimeSeconds(), slave, start, iterations);
        return new PartialResult(watch.getTotalTimeSeconds(), slave, start, iterations);
    }
}
