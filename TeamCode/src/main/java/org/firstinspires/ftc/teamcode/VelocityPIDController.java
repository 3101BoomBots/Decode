package org.firstinspires.ftc.teamcode;

import static com.pedropathing.math.MathFunctions.clamp;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

// CUSTOM PID controller: won't need unless the built-in controller proves to suck
public class VelocityPIDController {
    DcMotorEx motor;
    double prevError = 0;
    double summedError = 0;
    double previousMeasurement = 0;
    double previousError = 0;
    ElapsedTime time;
    ElapsedTime totalTime;
    PIDFCoefficients pid;
    public VelocityPIDController(DcMotorEx motor, PIDFCoefficients pid) {
        this.motor = motor;
        this.pid = pid;
        this.time = new ElapsedTime();
        this.totalTime = new ElapsedTime();
    }

    public double updatePID(double targetVelocity) {
        if (isVelocityCloseEnough(motor.getVelocity(), targetVelocity, 10)) return motor.getPower();
        double error = targetVelocity - motor.getVelocity();
        double dError = (error - this.prevError) / time.seconds();
        summedError += (error * time.seconds());  // ensures integral decays over time
        summedError *= Math.pow(Math.E, -totalTime.seconds() * time.seconds());
        double output = pid.p*error + pid.i*summedError + pid.d*dError;
        this.prevError = error;
        time.reset();
        this.motor.setPower(output);
        return output;
    }

    public void setPIDF(PIDFCoefficients pid) {
        this.pid = pid;
    }

    public double updatePIDFormula(double currentVelocity, double targetVelocity, double dt) {
        double error = targetVelocity - currentVelocity;
        final double integralMax = 1;
        // Integral term
        summedError += error * dt;
        summedError = clamp(summedError, -integralMax, integralMax);

        // Derivative on measurement (recommended for velocity control)
        double derivative = -(currentVelocity - previousMeasurement) / dt;

        double output =
                pid.p * error +
                        pid.i * summedError +
                        pid.d * derivative;

        previousError = error;
        previousMeasurement = currentVelocity;
        motor.setPower(output);
        return clamp(output, -1.0, 1.0);
    }


    private boolean isVelocityCloseEnough(double velocity, double target, double RpmAllowedError) {
        double lowerBound = target - RpmAllowedError;
        double upperBound = target + RpmAllowedError;
        return (velocity >= lowerBound) && (velocity <= upperBound);
    }
}
