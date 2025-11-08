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
            if(gamepad1.dpad_up) hw.nathanMikhailServo.setPosition(hw.nathanMikhailServo.getPosition() + 2);
            if(gamepad1.dpad_down) hw.nathanMikhailServo.setPosition(hw.nathanMikhailServo.getPosition() - 2);
            telemetry.addData("current pos", hw.nathanMikhailServo.getPosition());
            telemetry.update();
        }
    }
}
