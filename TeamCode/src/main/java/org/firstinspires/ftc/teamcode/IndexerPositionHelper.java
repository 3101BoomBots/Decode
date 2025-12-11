package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="indexer helper", group = "")
public class IndexerPositionHelper extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        Hardware hw = Hardware.getInstance(this);
        hw.init(hardwareMap);
        waitForStart();
        while(opModeIsActive()) {
//            if(gamepad1.a) hw.nathanMikhailServo.setPosition(3);
////            if(gamepad1.a) hw.nathanMikhailServo.setPosition(hw.nathanMikhailServo.getPosition() + 1);
////            if(gamepad1.b) hw.nathanMikhailServo.setPosition(hw.nathanMikhailServo.getPosition() - 1);
//            if(gamepad1.b) hw.nathanMikhailServo.setPosition(1);
//            telemetry.addData("current pos", hw.nathanMikhailServo.getPosition());
//            telemetry.update();
            hw.indexerMotor.setPower(0);
            hw.indexerMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            telemetry.addData("current", hw.indexerMotor.getCurrentPosition());
            telemetry.update();
        }
    }
}
