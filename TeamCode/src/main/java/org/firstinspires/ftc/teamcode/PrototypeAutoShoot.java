package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@Autonomous(name="protoShooter")
public class PrototypeAutoShoot extends LinearOpMode {
    Hardware hw = Hardware.getInstance(this);
    final double DEGREES_TO_GOAL = 35.0;

    @Override
    public void runOpMode() throws InterruptedException {
        hw.init(hardwareMap);
        hw.indexerMotor.setPower(1);
        waitForStart();
        if (isStopRequested()) return;
        telemetry.addData("heading", hw.pinpoint.getHeading(AngleUnit.DEGREES));
        telemetry.update();
        while (hw.pinpoint.getHeading(AngleUnit.DEGREES) < DEGREES_TO_GOAL && opModeIsActive()) {
            hw.backRight.setPower(-0.3);
            hw.frontRight.setPower(-0.3);
            hw.backLeft.setPower(0.3);
            hw.frontLeft.setPower(0.3);
            hw.pinpoint.update();
            telemetry.addData("heading", hw.pinpoint.getHeading(AngleUnit.DEGREES));
            telemetry.update();
        }
        hw.intakeMotor.setPower(1);
        hw.outtake(1180);
        sleep(5000); // time to reach velocity and stabilize PID
        for(int i = 0 ; i < 3; i++) {
            hw.indexerDown(1);
            while(hw.indexerMotor.isBusy()) {}
            hw.indexerUp(2);
            while(hw.indexerMotor.isBusy()) {}
        }
        while(hw.indexerMotor.isBusy()) {}
        while (opModeIsActive()) {
            hw.setPower(0.2);
        }
    }
}
