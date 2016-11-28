package misosw.pi;

import java.math.BigDecimal;

/**
 * Created by quirell on 27.11.2016.
 */
public class PartialResult {

    private BigDecimal sum;
    private double duration;
    private String slave;
    private int start;
    private int iterations;

    public PartialResult(BigDecimal sum, double duration, String slave, int start, int iterations) {
        this.sum = sum;
        this.duration = duration;
        this.slave = slave;
        this.start = start;
        this.iterations = iterations;
    }

    public PartialResult(double duration, String slave, int start, int iterations) {
        this.duration = duration;
        this.slave = slave;
        this.start = start;
        this.iterations = iterations;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public double getDuration() {
        return duration;
    }

    public String getSlave() {
        return slave;
    }

    public int getStart() {
        return start;
    }

    public int getIterations() {
        return iterations;
    }

    public boolean failure(){
        return sum == null;
    }
}
