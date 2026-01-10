package org.firstinspires.ftc.teamcode;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

@Configurable
@TeleOp(name = "outtakeTuning")
public class outtakeTuning extends LinearOpMode {
    // these should be editable on panels on the fly to tune PID
    public static double p = 74;
    public static double i = 0;
    public static double d = 0;
    public static double f = 0;
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotorEx outtakeMotorRight, outtakeMotorLeft;
        PIDFCoefficients pidfCoefficients = new PIDFCoefficients(p, i, d, f);
        TelemetryManager panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        double velocityRight, velocityLeft, targetVelocity = 0;

        outtakeMotorRight = hardwareMap.get(DcMotorEx.class, "outtakeMotorRight");
        outtakeMotorRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        outtakeMotorRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        outtakeMotorRight.setDirection(DcMotorSimple.Direction.REVERSE);

        outtakeMotorLeft = hardwareMap.get(DcMotorEx.class, "outtakeMotorLeft");
        outtakeMotorLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        outtakeMotorLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        outtakeMotorRight.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);
        outtakeMotorLeft.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);

//        outtakeMotorLeft.setPower(1);
//        outtakeMotorRight.setPower(1);
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

        while(opModeIsActive()) {
            if(gamepad1.a) {
                outtakeMotorLeft.setVelocity(880);
                outtakeMotorRight.setVelocity(880);
                targetVelocity = 880;
            }
            if(gamepad1.b) {
                outtakeMotorLeft.setVelocity(1180);
                outtakeMotorRight.setVelocity(1180);
                targetVelocity = 1180;
            }
            if(gamepad1.x) {
                outtakeMotorLeft.setVelocity(0);
                outtakeMotorRight.setVelocity(0);
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
            outtakeMotorRight.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);
            outtakeMotorLeft.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);
            panelsTelemetry.addData("Current PID", pidfCoefficients);

            panelsTelemetry.update(telemetry);
        }
    }
}
