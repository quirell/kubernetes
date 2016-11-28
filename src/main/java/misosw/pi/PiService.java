package misosw.pi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by quirell on 26.11.2016.
 */

@Service
public class PiService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${SLAVES_LIST}")
    private List<String> slaves;

    @Value("${MASTER}")
    private boolean isMaster;

    @Value("${pi.batch}")
    private int batch;
    @Value("${pi.computation.time}")
    private int computationTime;
    private static final int CONVERGENCE_SPEED = 3;

    @Autowired
    private CommunicationService communication;
    @Autowired
    private CompletionService<PartialResult> completionService;

    public BigDecimal bellardsFormula(int start, int iterations, int precision) {
        logger.info("Received partial sum computation request start {} iters {} precision {}", start, iterations, precision);
        BigDecimal sum = new BigDecimal(0);
        for (int i = start; i < iterations + start; i++) {
            BigDecimal tmp;
            BigDecimal term;
            BigDecimal divisor;
            term = new BigDecimal(-32);
            divisor = new BigDecimal(4 * i + 1);
            tmp = term.divide(divisor, precision, BigDecimal.ROUND_FLOOR);
            term = new BigDecimal(-1);
            divisor = new BigDecimal(4 * i + 3);
            tmp = tmp.add(term.divide(divisor, precision, BigDecimal.ROUND_FLOOR));
            term = new BigDecimal(256);
            divisor = new BigDecimal(10 * i + 1);
            tmp = tmp.add(term.divide(divisor, precision, BigDecimal.ROUND_FLOOR));
            term = new BigDecimal(-64);
            divisor = new BigDecimal(10 * i + 3);
            tmp = tmp.add(term.divide(divisor, precision, BigDecimal.ROUND_FLOOR));
            term = new BigDecimal(-4);
            divisor = new BigDecimal(10 * i + 5);
            tmp = tmp.add(term.divide(divisor, precision, BigDecimal.ROUND_FLOOR));
            term = new BigDecimal(-4);
            divisor = new BigDecimal(10 * i + 7);
            tmp = tmp.add(term.divide(divisor, precision, BigDecimal.ROUND_FLOOR));
            term = new BigDecimal(1);
            divisor = new BigDecimal(10 * i + 9);
            tmp = tmp.add(term.divide(divisor, precision, BigDecimal.ROUND_FLOOR));
            int s = ((1 - ((i & 1) << 1)));
            divisor = new BigDecimal(2);
            divisor = divisor.pow(10 * i).multiply(new BigDecimal(s));
            sum = sum.add(tmp.divide(divisor, precision, BigDecimal.ROUND_FLOOR));
        }
        return sum;
    }

    public String computePi(int precision) {
        logger.info("Received computation request precision {}", precision);
        int iterations = precision / CONVERGENCE_SPEED;
        int nextBatch = batch;
        int nextIteration = 0;
        LinkedList<String> free = new LinkedList<>(slaves);
        BigDecimal sum = new BigDecimal(0, new MathContext(precision));
        while (nextIteration < iterations || free.size() != slaves.size()) {
            Future<PartialResult> future;
            if (nextIteration >= iterations || free.isEmpty()) {
                try {
                    future = completionService.take();
                } catch (InterruptedException e) {
                    throw new Error("A worker thread has been interrupted, quitting", e);
                }
            } else {
                future = completionService.poll();
            }
            if (future != null) {
                try {
                    PartialResult part = future.get();
                    free.addLast(part.getSlave());
                    if (part.failure()) {
                        completionService.submit(() -> communication.getPartialSum(free.removeFirst(), part.getStart(), part.getIterations(), precision));
                    } else {
                        if (part.getDuration() > computationTime) {
                            nextBatch = part.getIterations() / 2;
                        } else {
                            nextBatch = part.getIterations() * 2;
                        }
                        sum = sum.add(part.getSum());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new Error("A worker thread has been interrupted, quitting", e);
                }
            }
            if (nextIteration < iterations) {
                final int nextIterationCopy = nextIteration;
                final int nextBatchCopy = nextBatch;
                final String slave = free.removeFirst();
                completionService.submit(() -> communication.getPartialSum(slave, nextIterationCopy, nextBatchCopy, precision));
                nextIteration += nextBatch;
            }

        }
        logger.info("Computation completed precision {}", precision);
        return sum.divide(new BigDecimal(64), precision, BigDecimal.ROUND_FLOOR).toPlainString();
    }

}
