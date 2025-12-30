package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name="McanumTester")
public class MecanumTester extends LinearOpMode {


    @Override
    public void runOpMode() throws InterruptedException {
        Hardware hw = Hardware.getInstance(this);
        hw.init(hardwareMap);
        hw.setWithEncoder();
        waitForStart();
        while (opModeIsActive()) {
            telemetry.addData("front left", hw.frontLeft.getCurrentPosition());
            telemetry.addData("front right", hw.frontRight.getCurrentPosition());
            telemetry.addData("back left", hw.backLeft.getCurrentPosition());
            telemetry.addData("back right", hw.backRight.getCurrentPosition());
            telemetry.addData("imu", hw.pinpoint.getHeading(AngleUnit.DEGREES));
            telemetry.update();

        }
    }
}


