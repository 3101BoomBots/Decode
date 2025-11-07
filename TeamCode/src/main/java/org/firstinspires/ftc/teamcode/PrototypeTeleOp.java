package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name="prototype")
public class PrototypeTeleOp extends LinearOpMode {


    @Override
    public void runOpMode() throws InterruptedException {
        Hardware hw = Hardware.getInstance(this);
        int timesRotated = 0;
        int intakeMode = 0;
        hw.init(hardwareMap);

        waitForStart();
        if (isStopRequested()) return;
        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);

            hw.frontLeft.setPower((y + x + rx) / denominator);
            hw.backLeft.setPower((y - x + rx) / denominator);
            hw.frontRight.setPower((y - x - rx) / denominator);
            hw.backRight.setPower((y + x - rx) / denominator);
            if(gamepad2.dpad_up) {
                //flap up
                hw.nathanMikhailServo.setPosition(hw.OPEN_POSITION_FLAP);
            }
            if (gamepad2.dpad_down) {
                // flap down
                hw.nathanMikhailServo.setPosition(hw.CLOSED_POSITION_FLAP);
            }

            // start at 0, rotate once, add one
            // timesRotated = 1, rotate another time, add one
            // timesRotated = 2, try to rotate another - wouldn't work, stuck to go back one
            // timesRotated = 1
            // starts at 0, rotate back once, timesRotated = -1

            if (gamepad2.dpad_right) {
                if (timesRotated != 2) {
                    hw.indexerMotor.setTargetPosition(hw.indexerMotor.getTargetPosition() + (int) (0.333 * hw.INDEXER_RESOLUTION));
                    timesRotated = timesRotated + 1;
                }
            }
            if (gamepad2.dpad_left) {
                if (timesRotated != 0) {
                    hw.indexerMotor.setTargetPosition(hw.indexerMotor.getTargetPosition() - (int) (0.333 * hw.INDEXER_RESOLUTION));
                    timesRotated = timesRotated - 1;
                }
            }

            // intake/ outtake
            // intake left trigger
            if(gamepad2.left_bumper) {
                if(intakeMode == 2) {
                    intakeMode = 0;
                }
                intakeMode = intakeMode + 1;
            }

            if(intakeMode == 0) {
                hw.intakeMotor.setPower(0);
            } else if (intakeMode == 1) {
                hw.intakeMotor.setPower(-1);
            } else {
                hw.intakeMotor.setPower(1);
            }
            // outtake right trigger
            if (gamepad2.right_trigger > 0.1) {
                hw.outtakeMotorLeft.setPower(1);
                hw.outtakeMotorRight.setPower(1);
            }
        }
    }
}
