package org.firstinspires.ftc.teamcode;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

@Configurable
@TeleOp(name = "outtakeTuningCustom")
public class outtakeTuningCustom extends LinearOpMode {
    // these should be editable on panels on the fly to tune PID
    public static double p = 1;
    public static double i = 0;
    public static double d = 0;
    public static double f = 0;
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotorEx outtakeMotorRight, outtakeMotorLeft;
        PIDFCoefficients pidfCoefficients = new PIDFCoefficients(p, i, d, f);
        TelemetryManager panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        VelocityPIDController rightPID, leftPID;
        double velocityRight, velocityLeft, targetVelocity = 0;

        outtakeMotorRight = hardwareMap.get(DcMotorEx.class, "outtakeMotorRight");
        outtakeMotorRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        outtakeMotorRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        outtakeMotorRight.setDirection(DcMotorSimple.Direction.REVERSE);

        outtakeMotorLeft = hardwareMap.get(DcMotorEx.class, "outtakeMotorLeft");
        outtakeMotorLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        outtakeMotorLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        leftPID = new VelocityPIDController(outtakeMotorLeft, pidfCoefficients);
        rightPID = new VelocityPIDController(outtakeMotorRight, pidfCoefficients);

        panelsTelemetry.debug("Init was ran!");
        panelsTelemetry.update(telemetry);

        panelsTelemetry.addData("Info", "This teleop will set the velocity to 880 RPM when A is " +
                "pressed which is the velocity set for the short shooting. Observe the graph in " +
                "panels (accessible at 192.168.43.1:8001 when connected to the robot's wifi) and " +
                "tune accordingly. The same process will repeat for a velocity of 1180 RPM when B " +
                "is pressed. If X is pressed then the velocity will be set to 0. The goal is to " +
                "minimize oscillation and error while maximizing speed for all of these velocities " +
                "(1180, 880, and 0). ");
        panelsTelemetry.addData("Current PID", pidfCoefficients);
        panelsTelemetry.update(telemetry);

        waitForStart();
        ElapsedTime time = new ElapsedTime();
        while(opModeIsActive()) {
            if(gamepad1.a) {
                targetVelocity = 880;
            }
            if(gamepad1.b) {
                targetVelocity = 1180;
            }
            if(gamepad1.x) {
                targetVelocity = 0;
            }
            velocityRight = outtakeMotorRight.getVelocity();
            velocityLeft = outtakeMotorLeft.getVelocity();

            panelsTelemetry.addData("outtakeVelocityRight", velocityRight);
            panelsTelemetry.addData("outtakeVelocityLeft", velocityLeft);
            panelsTelemetry.addData("left error", targetVelocity - velocityLeft);
            panelsTelemetry.addData("right error", targetVelocity - velocityRight);
            panelsTelemetry.addData("zero line", 0);
            panelsTelemetry.addData("targetVelocity", targetVelocity);

            pidfCoefficients = new PIDFCoefficients(p, i, d, f);

            leftPID.setPIDF(pidfCoefficients);
            rightPID.setPIDF(pidfCoefficients);
            panelsTelemetry.addData("Current PID", pidfCoefficients);
            leftPID.updatePIDFormula(outtakeMotorLeft.getVelocity(), targetVelocity, time.seconds());
            rightPID.updatePIDFormula(outtakeMotorRight.getVelocity(), targetVelocity, time.seconds());
            time.reset();
            panelsTelemetry.update(telemetry);
        }
    }
}
