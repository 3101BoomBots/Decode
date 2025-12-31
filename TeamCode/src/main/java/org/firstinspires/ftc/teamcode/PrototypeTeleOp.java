package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;


import java.util.Arrays;
import java.util.List;


@TeleOp(name="prototype")
public class PrototypeTeleOp extends LinearOpMode {
    int position;

    @Override
    public void runOpMode() throws InterruptedException {
        Hardware hw = Hardware.getInstance(this);
//        final double MAX_VELOCITY_OUTTAKE = (double)3000/1;
//        final double MIN_VELOCITY_OUTTAKE = 0.4;
        boolean isStepByStep = false;
        boolean isIndexerActive = false;
//        boolean dpadRightDown = false;
//        boolean dpadLeftDown = false;
        int lastCurrentPosition = 0;
//        telemetry.setAutoClear(false);

//        int timesRotated = 0;
        hw.init(hardwareMap);
        hw.setPower(0);
//        hw.setToNoEncoder();

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

            if(!hw.indexerMotor.isBusy()) isIndexerActive = false;

            //reset
            if(gamepad2.dpad_up) {
                hw.indexerMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                hw.indexerMotor.setPower(0.8);
                hw.indexerMotor.setTargetPosition(0);
                hw.indexerMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            }
            if (!isStepByStep) {
                if (gamepad2.dpad_right) {
                    hw.indexerMotor.setTargetPosition(0);
                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() + (10 * Hardware.INDEXER_RESOLUTION)));
                    isIndexerActive = true;
//                    hw.indexerMotor.setPower(1);
                }
                if (gamepad2.dpad_left) {
                    hw.indexerMotor.setTargetPosition(0);
                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() - (10 * Hardware.INDEXER_RESOLUTION)));
                    isIndexerActive = true;
//                    hw.indexerMotor.setPower(-1);
                }
                if (gamepad2.dpad_down) {
//                    hw.indexerMotor.setPower(0);
                    hw.indexerMotor.setTargetPosition(hw.indexerMotor.getCurrentPosition());
                    isIndexerActive = false;
                }
            }
            else if(isStepByStep) {
                if (gamepad2.dpadRightWasPressed()) {
                    if(position != 2) position++;
                    else position = 0;
                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() + (0.333 * Hardware.INDEXER_RESOLUTION)));
                    isIndexerActive = true;
//                    hw.indexerMotor.setPower(1);
                }
                if (gamepad2.dpadLeftWasPressed()) { // down
                    if (position != 0) position--;
                    else position = 2;
                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() - (0.333 * Hardware.INDEXER_RESOLUTION)));
                    isIndexerActive = true;
//                    hw.indexerMotor.setPower(-1);
                }
                if (gamepad2.dpad_down) {
                    hw.indexerMotor.setTargetPosition(hw.indexerMotor.getCurrentPosition());
                    isIndexerActive = false;
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
            //htn = nathan
            // outtake right trigger
            if (gamepad2.right_trigger > 0.1) {
//                double velocity = MIN_VELOCITY_OUTTAKE + (MAX_VELOCITY_OUTTAKE - MIN_VELOCITY_OUTTAKE) * gamepad1.right_trigger;
                hw.outtake(880);
                hw.indexerMotor.setTargetPosition(closestPosition(hw.indexerMotor.getCurrentPosition(), (int)Hardware.INDEXER_RESOLUTION));
                isStepByStep = true;
            } if (gamepad2.right_bumper) {  // long
                hw.outtake(1180);
                int closesPos = closestPosition(hw.indexerMotor.getCurrentPosition(), (int)Hardware.INDEXER_RESOLUTION);
                telemetry.addData("closestPos", closesPos);
                hw.indexerMotor.setTargetPosition(closesPos);
                isStepByStep = true;
            } if (gamepad2.left_trigger > 0.1 ) {
                hw.outtakeMotorRight.setVelocity(0);
                hw.outtakeMotorLeft.setVelocity(0);
                isStepByStep = false;
            } if (Math.abs(hw.indexerMotor.getCurrentPosition() - lastCurrentPosition) < 10 &&
                    hw.indexerMotor.getPower() != 0 && isIndexerActive && hw.indexerMotor.isBusy() &&
            hw.indexerMotor.isOverCurrent()) {
//                    telemetry.addData("STUCK BALL", "");
//                    telemetry.addData("power", hw.indexerMotor.getPower());
//                    telemetry.addData("isbusy", hw.indexerMotor.isBusy());
//                    telemetry.addData("isovercurrent", hw.indexerMotor.isOverCurrent());
//                    telemetry.addData("current", hw.indexerMotor.getCurrent(CurrentUnit.AMPS));
//                    telemetry.update();

                hw.indexerMotor.setTargetPosition(hw.indexerMotor.getCurrentPosition() - 100);
            }

            lastCurrentPosition = hw.indexerMotor.getCurrentPosition();
//            telemetry.addData("downleft", dpadLeftDown);
//            telemetry.addData("downright", dpadRightDown);
            telemetry.addData("velocity right", hw.outtakeMotorRight.getVelocity());
            telemetry.addData("velocity left", hw.outtakeMotorLeft.getVelocity());
            telemetry.addData("pos", hw.indexerMotor.getCurrentPosition());
            telemetry.addData("target", hw.indexerMotor.getTargetPosition());
            telemetry.addData("indexerPower", hw.indexerMotor.getPower());
            telemetry.update();
        }
    }
//briggs
    private int closestPosition(int currPos, int indexerResolution) {
        int[] positions = generatePositions(currPos);
        Integer[] differences = new Integer[]{indexerResolution - (currPos - positions[0]),
                indexerResolution - (currPos - positions[1]), indexerResolution - (currPos - positions[2])};
        List<Integer> differenceToPos = Arrays.asList(differences);
        int minDiff = Math.min(Math.min(differenceToPos.get(0), differenceToPos.get(1)), differenceToPos.get(2));
        int indexOfMin = differenceToPos.indexOf(minDiff);
        position = indexOfMin;
        return positions[indexOfMin];
    }

    int[] generatePositions(int currPos) {
        return new int[]{0 + currPos, (int) (0.333 * Hardware.INDEXER_RESOLUTION) + currPos, (int) (0.666 * Hardware.INDEXER_RESOLUTION) + currPos};
    }
}
