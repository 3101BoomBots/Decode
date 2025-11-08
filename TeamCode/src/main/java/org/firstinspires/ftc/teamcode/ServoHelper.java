package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="servo helper", group = "")
public class ServoHelper extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        Hardware hw = Hardware.getInstance(this);
        hw.init(hardwareMap);
        waitForStart();
        while(opModeIsActive()) {
            if(gamepad1.a) hw.nathanMikhailServo.setPosition(3);
//            if(gamepad1.a) hw.nathanMikhailServo.setPosition(hw.nathanMikhailServo.getPosition() + 1);
//            if(gamepad1.b) hw.nathanMikhailServo.setPosition(hw.nathanMikhailServo.getPosition() - 1);
            if(gamepad1.b) hw.nathanMikhailServo.setPosition(1);
            telemetry.addData("current pos", hw.nathanMikhailServo.getPosition());
            telemetry.update();
        }
    }
}
