import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

// CUSTOM PID controller: won't need unless the built-in controller proves to suck
public class VelocityPIDController {
    DcMotorEx motor;
    double prevError = 0;
    double summedError = 0;
    ElapsedTime time;
    PIDFCoefficients pid;
    public VelocityPIDController(DcMotorEx motor, PIDFCoefficients pid) {
        this.motor = motor;
        this.pid = pid;
        this.time = new ElapsedTime();
    }

    public double updatePID(double targetVelocity) {
        if (isVelocityCloseEnough(motor.getVelocity(), targetVelocity, 10)) return motor.getPower();
        double error = targetVelocity - motor.getVelocity();
        double dError = (error - this.prevError) / time.time();
        summedError += (error * time.time());
        double output = pid.p*error + pid.i*summedError + pid.d*dError;
        this.prevError = error;
        time.reset();
        this.motor.setPower(output);
        return output;
    }

    private boolean isVelocityCloseEnough(double velocity, double target, double RpmAllowedError) {
        double lowerBound = target - RpmAllowedError;
        double upperBound = target + RpmAllowedError;
        return (velocity >= lowerBound) && (velocity <= upperBound);
    }
}
