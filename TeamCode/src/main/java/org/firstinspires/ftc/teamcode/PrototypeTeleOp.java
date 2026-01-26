package org.firstinspires.ftc.teamcode;

import com.bylazar.panels.Panels;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;


import java.util.Arrays;
import java.util.List;


@TeleOp(name="prototype")
public class PrototypeTeleOp extends LinearOpMode {
    int position;

    @Override
    public void runOpMode() throws InterruptedException {
        Hardware hw = Hardware.getInstance(this);

        boolean isStepByStep = false;
        final double SLOW_POWER = 0.4;
        int pos = 0;
        boolean isSlow = false;
        TelemetryManager panels = PanelsTelemetry.INSTANCE.getTelemetry();

//        int timesRotated = 0;
        hw.init(hardwareMap);
        hw.setPower(0);

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

            if(!isSlow) {
                hw.frontLeft.setPower((y + x + rx) / denominator);
                hw.backLeft.setPower((y - x + rx) / denominator);
                hw.frontRight.setPower((y - x - rx) / denominator);
                hw.backRight.setPower((y + x - rx) / denominator);
            }

            if (gamepad1.dpad_right) {
                isSlow = true;
                hw.frontLeft.setPower(SLOW_POWER);
                hw.backLeft.setPower(SLOW_POWER);
                hw.frontRight.setPower(-SLOW_POWER);
                hw.backRight.setPower(-SLOW_POWER);
            } else if (gamepad1.dpad_left) {
                isSlow = true;
                hw.frontLeft.setPower(-SLOW_POWER);
                hw.backLeft.setPower(-SLOW_POWER);
                hw.frontRight.setPower(SLOW_POWER);
                hw.backRight.setPower(SLOW_POWER);
            } else {
                isSlow = false;
            }

            //reset
//            if(gamepad2.dpad_up) {
//                hw.indexerMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//                hw.indexerMotor.setPower(1);
//                hw.indexerMotor.setTargetPosition(0);
//                hw.indexerMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//            }
            if (!isStepByStep) {
                if (gamepad2.dpad_right) {
//                    hw.indexerMotor.setTargetPosition(0);
//                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() + (10 * Hardware.INDEXER_RESOLUTION)));
                    hw.indexerMotor.setVelocity(2500);
                }
                if (gamepad2.dpad_left) {
//                    hw.indexerMotor.setTargetPosition(0);
//                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() - (10 * Hardware.INDEXER_RESOLUTION)));
                    hw.indexerMotor.setVelocity(-2500);
                }
                if (gamepad2.dpad_down) {
                    hw.indexerMotor.setVelocity(0);
//                    hw.indexerMotor.setTargetPosition(hw.indexerMotor.getCurrentPosition());
                }
            }
            else if(isStepByStep) {
                if (gamepad2.dpadRightWasPressed()) {
                    if(position != 2) position++;
                    else position = 0;
                    pos = hw.velocityIndexerUp();
//                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() + (0.333 * Hardware.INDEXER_RESOLUTION)));
                }
                if (gamepad2.dpadLeftWasPressed()) { // down
                    if (position != 0) position--;
                    else position = 2;
                    pos = hw.velocityIndexerDown();
                }
                if (gamepad2.dpad_down || Math.abs(pos - hw.indexerMotor.getCurrentPosition()) <= 20
                || Math.abs(pos - hw.indexerMotor.getCurrentPosition()) >= 200) {
                    hw.indexerMotor.setVelocity(0);
//                    hw.indexerMotor.setTargetPosition(hw.indexerMotor.getCurrentPosition());
                }
            }
            // intake/ outtake
            if(gamepad2.aWasPressed()) {
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
                hw.outtake(880);
                resetPosition(hw);
                isStepByStep = true;
            } if (gamepad2.right_bumper) {  // long
                hw.outtake(1180);
                resetPosition(hw);
                isStepByStep = true;
            } if (gamepad2.left_trigger > 0.1) {
                hw.outtakeMotorRight.setVelocity(0);
                hw.outtakeMotorLeft.setVelocity(0);
                hw.indexerVelocity();
                isStepByStep = false;
            }

            panels.addData("velocity right", hw.outtakeMotorRight.getVelocity());
            panels.addData("velocity left", hw.outtakeMotorLeft.getVelocity());
//            panels.addData("pos", hw.indexerMotor.getCurrentPosition());
//            panels.addData("target", hw.indexerMotor.getTargetPosition());
            panels.addData("indexer speed", Math.abs(hw.indexerMotor.getVelocity()));
            panels.addData("indexer error", Math.abs(hw.indexerMotor.getCurrentPosition() - pos));
            panels.update(telemetry);
        }
    }

    private int closestPosition(int currPos, int indexerResolution) {
        int[] positions = generatePositions(currPos);
        Integer[] differences = new Integer[]{
                indexerResolution - (currPos - positions[0]),
                indexerResolution - (currPos - positions[1]),
                indexerResolution - (currPos - positions[2])};
        List<Integer> differenceToPos = Arrays.asList(differences);
        int minDiff = Math.min(Math.min(differenceToPos.get(0), differenceToPos.get(1)), differenceToPos.get(2));
        int indexOfMin = differenceToPos.indexOf(minDiff);
        position = indexOfMin;
        return positions[indexOfMin];
    }

    private int[] generatePositions(int currPos) {
        return new int[]{0 + currPos, (int) (0.333 * Hardware.INDEXER_RESOLUTION) + currPos, (int) (0.666 * Hardware.INDEXER_RESOLUTION) + currPos};
    }

    private void resetPosition(Hardware hw) {
        int closesPos = closestPosition(hw.indexerMotor.getCurrentPosition(), (int)Hardware.INDEXER_RESOLUTION);
        if(!hw.indexerMotor.getMode().equals(DcMotor.RunMode.RUN_TO_POSITION)) hw.indexerRun();
        hw.indexerMotor.setTargetPosition(closesPos);
        hw.indexerMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hw.indexerVelocity();
    }
}
