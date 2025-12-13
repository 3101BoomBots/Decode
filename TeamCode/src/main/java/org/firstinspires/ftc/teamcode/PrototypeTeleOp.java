package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TeleOp(name="prototype")
public class PrototypeTeleOp extends LinearOpMode {
    int[] positions = {0, (int)(0.333 * Hardware.INDEXER_RESOLUTION), (int)(0.666 * Hardware.INDEXER_RESOLUTION)};
    int position;

    @Override
    public void runOpMode() throws InterruptedException {
        Hardware hw = Hardware.getInstance(this);
//        final double MAX_VELOCITY_OUTTAKE = (double)3000/1;
//        final double MIN_VELOCITY_OUTTAKE = 0.4;
        boolean isStepByStep = false;
        boolean isIndexerActive = false;
        boolean dpadRightDown = false;
        boolean dpadLeftDown = false;
        int lastCurrentPosition = 0;
//        telemetry.setAutoClear(true);

        int timesRotated = 0;
        hw.init(hardwareMap);

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
            // start at 0, rotate once, add one
            // timesRotated = 1, rotate another time, add one
            // timesRotated = 2,
            // starts at 0, rotate back once, timesRotated = 2
            if (!isStepByStep) {
                if (gamepad2.dpad_right) {
                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() + (0.333 * Hardware.INDEXER_RESOLUTION)));
                    isIndexerActive = true;
//                    hw.indexerMotor.setPower(1);
                }
                if (gamepad2.dpad_left) {
                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() - (0.333 * Hardware.INDEXER_RESOLUTION)));
                    isIndexerActive = true;
//                    hw.indexerMotor.setPower(-1);
                }
                if (gamepad2.dpad_down) {
                    hw.indexerMotor.setPower(0);
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
                if (gamepad2.dpadLeftWasPressed()) {
                    if (position != 0) position--;
                    else position = 2;
                    hw.indexerMotor.setTargetPosition((int)(hw.indexerMotor.getTargetPosition() - (0.333 * Hardware.INDEXER_RESOLUTION)));
                    isIndexerActive = true;
//                    hw.indexerMotor.setPower(-1);
                }
                if (gamepad2.dpad_down) {
                    hw.indexerMotor.setPower(0);
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
                double velocity = 2500;
                hw.outtakeMotorLeft.setVelocity(velocity);
                hw.outtakeMotorRight.setVelocity(velocity);
                int closesPos = navigateToClosesPosition(hw.indexerMotor.getCurrentPosition(), (int)hw.INDEXER_RESOLUTION);
                telemetry.addData("closestPos", closesPos);
                hw.indexerMotor.setTargetPosition(closesPos);
//                hw.outtakeMotorLeft.setPower(0.4);
//                hw.outtakeMotorRight.setPower(0.4);
                isStepByStep = true;
            } if (gamepad2.left_trigger > 0.1 ) {
                hw.outtakeMotorRight.setVelocity(0);
                hw.outtakeMotorLeft.setVelocity(0);
                isStepByStep = false;
            } if (Math.abs(hw.indexerMotor.getCurrentPosition() - lastCurrentPosition) < 20 &&
                    hw.indexerMotor.getPower() != 0 && isIndexerActive) {
                for(int i = 0; i <3; i++) {
                    telemetry.addData("STUCK BALL", "NOW");
                    telemetry.addData("power", hw.indexerMotor.getPower());
                }
                telemetry.update();
                hw.indexerMotor.setTargetPosition(hw.indexerMotor.getCurrentPosition() - 30);
            }

            lastCurrentPosition = hw.indexerMotor.getCurrentPosition();
            telemetry.addData("downleft", dpadLeftDown);
            telemetry.addData("downright", dpadRightDown);
            telemetry.addData("velocity right", hw.outtakeMotorRight.getVelocity());
            telemetry.addData("velocity left", hw.outtakeMotorLeft.getVelocity());
            telemetry.addData("pos", hw.indexerMotor.getCurrentPosition());
            telemetry.update();
        }
    }
//briggs
    private int navigateToClosesPosition(int currPos, int indexerResolution) {
        Integer[] differences = new Integer[]{indexerResolution - (currPos - positions[0]),
                indexerResolution - (currPos - positions[1]), indexerResolution - (currPos - positions[2])};
        List<Integer> differenceToPos = Arrays.asList(differences);
        int minDiff = Math.min(Math.min(differenceToPos.get(0), differenceToPos.get(1)), differenceToPos.get(2));
        int indexOfMin = differenceToPos.indexOf(minDiff);
        position = indexOfMin;
        return positions[indexOfMin];
    }
}
