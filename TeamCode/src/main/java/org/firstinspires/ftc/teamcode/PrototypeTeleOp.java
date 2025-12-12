package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import java.util.HashMap;
import java.util.Map;

@TeleOp(name="prototype")
public class PrototypeTeleOp extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        Hardware hw = Hardware.getInstance(this);
        final double MAX_VELOCITY_OUTTAKE = (double)3000/1;
        final double MIN_VELOCITY_OUTTAKE = 0.4;
        boolean dpadRightDown = false;
        boolean dpadLeftDown = false;
        int position = 0;
//        int[] positions = {0};

        int timesRotated = 0;
        hw.init(hardwareMap);
//        telemetry.setAutoClear(false);

        waitForStart();
        if (isStopRequested()) return;
        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = -gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);

            hw.frontLeft.setPower((y + x + rx) / denominator);
            hw.backLeft.setPower((y - x + rx) / denominator);
            hw.frontRight.setPower((y - x - rx) / denominator);
            hw.backRight.setPower((y + x - rx) / denominator);

            // start at 0, rotate once, add one
            // timesRotated = 1, rotate another time, add one
            // timesRotated = 2,
            // starts at 0, rotate back once, timesRotated = 2
            if (gamepad2.dpad_right) {
                hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() + (0.333 * hw.INDEXER_RESOLUTION)));
            }
            if (gamepad2.dpad_left) {
                hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() - (0.333 * hw.INDEXER_RESOLUTION)));
            }

            // intake/ outtake
            if(gamepad2.a) {
                hw.intakeMotor.setPower(-1);
            }
            if(gamepad2.b) {
                hw.intakeMotor.setPower(1);
            }
            if(gamepad2.x){
                hw.intakeMotor.setPower(0);
            }
            // outtake right trigger
            if (gamepad2.right_trigger > 0.1) {
//                double velocity = MIN_VELOCITY_OUTTAKE + (MAX_VELOCITY_OUTTAKE - MIN_VELOCITY_OUTTAKE) * gamepad1.right_trigger;
//                double velocity = MAX_VELOCITY_OUTTAKE;
//                hw.outtakeMotorLeft.setVelocity(velocity);
//                hw.outtakeMotorRight.setVelocity(velocity);
                hw.outtakeMotorLeft.setPower(1);
                hw.outtakeMotorRight.setPower(1);

//            } else {
//                hw.outtakeMotorLeft.setPower(0);
//                hw.outtakeMotorRight.setPower(0);
            } if (gamepad2.left_trigger > 0.1 ) {
                hw.outtakeMotorRight.setVelocity(0);
                hw.outtakeMotorLeft.setVelocity(0);
            }
            telemetry.addData("downleft", dpadLeftDown);
            telemetry.addData("downright", dpadRightDown);
            telemetry.addData("velocity right", hw.outtakeMotorRight.getVelocity());
            telemetry.addData("velocity left", hw.outtakeMotorLeft.getVelocity());
            telemetry.addData("pos", hw.indexerMotor.getCurrentPosition());
            telemetry.update();
        }
    }
}
