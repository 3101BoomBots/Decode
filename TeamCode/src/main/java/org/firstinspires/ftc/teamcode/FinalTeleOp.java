package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.HeadingInterpolator;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@TeleOp (name = "FINAL ULTIMATE TELEOP")
public class FinalTeleOp extends OpMode {
    private Follower follower;
    //    public static Pose startingPose; //See ExampleAuto to understand how to use this
    private boolean automatedDrive;
//    private Supplier<PathChain> pathChain;
    private boolean slowMode = false;
    private double slowModeMultiplier = 0.4;
    private Supplier<PathChain> pathChain;
    private Pose savedPosition = new Pose(45, 98);
    private Hardware hw;
    int position;
    int pos = 0;
    boolean isSpinning = false;
    boolean isStepByStep = false;

    BALL_STATUS positionOne = BALL_STATUS.PURPLE;
    BALL_STATUS positionTwo = BALL_STATUS.PURPLE;
    BALL_STATUS positionThree = BALL_STATUS.GREEN;
    BALL_STATUS ballColor;
    TelemetryManager panels;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose());
        follower.update();
        hw = Hardware.getInstance(this);
        hw.pedroInit(hardwareMap);
        panels = PanelsTelemetry.INSTANCE.getTelemetry();
        panels.setUpdateInterval(500);
        ballColor = positionOne;

        pathChain = () -> follower.pathBuilder() //Lazy Curve Generation
                .addPath(new Path(new BezierLine(follower::getPose, savedPosition)))
                .setHeadingInterpolation(HeadingInterpolator.linearFromPoint(follower::getHeading, savedPosition.getHeading(), 0.8))
                .build();
    }

    @Override
    public void start() {
        //The parameter controls whether the Follower should use break mode on the motors (using it is recommended).
        //In order to use float mode, add .useBrakeModeInTeleOp(true); to your Drivetrain Constants in Constant.java (for Mecanum)
        //If you don't pass anything in, it uses the default (false)
        follower.startTeleopDrive();
        ballColor = getColorInPositionOne(hw.color1, hw.color2);
        positionOne = ballColor;
    }

    @Override
    public void loop() {
        // Call this once per loop
        // drivetrain ==============
        follower.update();

        if (!automatedDrive) {
            //Make the last parameter false for field-centric
            //In case the drivers want to use a "slowMode" you can scale the vectors

            //This is the normal version to use in the TeleOp
            if (!slowMode) follower.setTeleOpDrive(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    false // Robot Centric
            );

            //This is how it looks with slowMode on
            else follower.setTeleOpDrive(
                    -gamepad1.left_stick_y * slowModeMultiplier,
                    -gamepad1.left_stick_x * slowModeMultiplier,
                    -gamepad1.right_stick_x * slowModeMultiplier,
                    false // Robot Centric
            );
        }

        //Automated PathFollowing
        if (gamepad1.aWasPressed()) {
            follower.followPath(pathChain.get());
            automatedDrive = true;
        }

        if(gamepad1.rightStickButtonWasPressed()) {
            savedPosition = follower.getPose();
        }

        //Stop automated following if the follower is done
        if (automatedDrive && (gamepad1.bWasPressed() || !follower.isBusy())) {
            follower.startTeleopDrive();
            automatedDrive = false;
        }

        //Slow Mode
        if (gamepad1.rightBumperWasPressed()) {
            slowMode = !slowMode;
        }

        //Optional way to change slow mode strength
        if (gamepad1.xWasPressed()) {
            slowModeMultiplier += 0.15;
        }

        //Optional way to change slow mode strength
        if (gamepad1.yWasPressed()) {
            slowModeMultiplier -= 0.15;
        }

        // intake ========
        if (gamepad2.yWasPressed()) {
            hw.intakeMotor.setPower(-1);
        }
        if (gamepad2.bWasPressed()) {
            hw.intakeMotor.setPower(1);
        }
        if (gamepad2.right_stick_button) {
            hw.intakeMotor.setPower(0);
        }

        // indexer =====
        if (!isStepByStep) {
            if (gamepad2.dpad_right) {
                hw.indexerMotor.setVelocity(1000);
            }
            if (gamepad2.dpad_left) {
                hw.indexerMotor.setVelocity(-2500);
            }
            if (gamepad2.dpadDownWasPressed()) {
                hw.indexerMotor.setVelocity(0);
            }
        } else {
            if (gamepad2.dpadRightWasPressed()) {
                indexerToIntake(1);
            }
            if (gamepad2.dpadLeftWasPressed()) { // down
                indexerToOuttake(1);
            }
            if (gamepad2.dpadDownWasPressed() || Math.abs(pos - hw.indexerMotor.getCurrentPosition()) <= 20
                    || Math.abs(pos - hw.indexerMotor.getCurrentPosition()) >= 200) {
                hw.indexerMotor.setVelocity(0);
            }
        }

        // outtake ====
        if (gamepad2.right_trigger > 0.1) {
            hw.outtake(880);
            resetPosition(hw);
            isStepByStep = true;
        } if (gamepad2.right_bumper) {  // long
            hw.outtake(1180);
            resetPosition(hw);
            isStepByStep = true;
        } if (gamepad2.left_trigger > 0.1) {
            hw.outtake(0);
            resetPosition(hw);
            hw.indexerVelocity();
            isStepByStep = false;
        }

        // color sensors ======
        ballColor = getColorInPositionOne(hw.color1, hw.color2);
        positionOne = ballColor;

        if (gamepad1.dpadRightWasPressed()) {
            indexerToOuttake(1);
        }
        if (gamepad1.dpadLeftWasPressed()) {
            indexerToIntake(1);
        }

        // auto color shooting ======
        if (gamepad2.aWasPressed()) {
            isStepByStep = true;
            int greenPosition = searchBallColor(BALL_STATUS.GREEN);
            if (greenPosition != 0) shootFromPosition(greenPosition);
        } else if (gamepad2.xWasPressed()) {
            isStepByStep = true;
            int purplePosition = searchBallColor(BALL_STATUS.PURPLE);
            if (purplePosition != 0) shootFromPosition(purplePosition);
        }

        panels.addData("position one", positionOne);
        panels.addData("position two", positionTwo);
        panels.addData("position three", positionThree);

        panels.addData("velocity right", hw.outtakeMotorRight.getVelocity());
        panels.addData("velocity left", hw.outtakeMotorLeft.getVelocity());
        panels.addData("indexer speed", Math.abs(hw.indexerMotor.getVelocity()));
        panels.addData("indexer error", Math.abs(hw.indexerMotor.getCurrentPosition() - pos));
//        panels.update(telemetry);
    }

    private void resetPosition(Hardware hw) {
        int closesPos = closestPosition(0, (int)Hardware.INDEXER_RESOLUTION);
        if(!hw.indexerMotor.getMode().equals(DcMotor.RunMode.RUN_TO_POSITION)) hw.indexerRun();
        hw.indexerMotor.setTargetPosition(closesPos);
        hw.indexerMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hw.indexerVelocity();
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


    private int searchBallColor(BALL_STATUS color) {
        if(positionThree.equals(color)) return 3;
        if(positionTwo.equals(color)) return 2;
        if(positionOne.equals(color)) return 1;
        return 0;
    }

    private void shootFromPosition(int position) {
        // 3 : 1 down 1 up
        // 2 : 1 up 1 down 1 up
        // 1 : 2 up 1 down 1 up
        switch (position) {
            case 3:
                shootAndWait(3, () -> indexerToOuttake(1), 31);
                break;
            case 31:
                shootAndWait(31, () -> indexerToIntake(1), 99);
                break;
            case 2:
                shootAndWait(2, () -> indexerToIntake(1), 21);
                break;
            case 21:
                shootAndWait(21, () -> indexerToOuttake(1), 22);
                break;
            case 22:
                shootAndWait(22, () -> indexerToIntake(1), 99);
                break;
            case 1:
                shootAndWait(1, () -> indexerToIntake(1), 11);
                break;
            case 11:
                shootAndWait(11, () -> indexerToIntake(1), 12);
                break;
            case 12:
                shootAndWait(12, () -> indexerToOuttake(1), 13);
                break;
            case 13:
                shootAndWait(13, () -> indexerToIntake(1), 99);
                break;
            case 99:  // designated base case
                hw.indexerMotor.setVelocity(0);
                break;
            default:
                throw new IllegalArgumentException("Target position is not 1, 2, or 3");
        }
    }

    private void shootAndWait(int initialCase, Runnable initialShot, int nextCase) {
        if(!isSpinning) {
            initialShot.run();
            isSpinning = true;
        }
        if (gamepad2.dpadDownWasPressed() || Math.abs(pos - hw.indexerMotor.getCurrentPosition()) <= 20
                || Math.abs(pos - hw.indexerMotor.getCurrentPosition()) >= 200) {
            hw.indexerMotor.setVelocity(0);
            isSpinning = false;
            panels.addData("run", "moving on to " + nextCase + " from " + initialCase );
            panels.update(telemetry);
            shootFromPosition(nextCase); // 3_1 is like 3.1, the next step for position 3
//            panels.addData("case", initialCase);
//            panels.addData("next case", nextCase);
//            panels.addData("pos", pos);
//            panels.addData("curr", hw.indexerMotor.getCurrentPosition());
        } else {
            shootFromPosition(initialCase);
//            panels.addData("run", "repeated");
//            panels.update(telemetry);
        }
    }


    private void indexerToOuttake(int rotations) {
        if (position != 0) position--;
        else position = 2;
        pos = hw.velocityIndexerDown();
        BALL_STATUS temp;
        for(int i = 0; i < rotations; i++) {
            temp = positionTwo;
            positionTwo = positionThree;
            positionThree = positionOne;
            positionOne = temp;
        }
        positionTwo = BALL_STATUS.NEITHER;  // position two neither because it gets launched
    }

    private void indexerToIntake(int rotations) {
        if(position != 2) position++;
        else position = 0;
        pos = hw.velocityIndexerUp();
        BALL_STATUS temp;
        for(int i = 0; i < rotations; i++) {
            temp = positionThree;
            positionThree = positionTwo;
            positionTwo = positionOne;
            positionOne = temp;
        }
    }

    private BALL_STATUS checkBall(float[] colorsHSV) {
        if (colorsHSV[2] < 0.004) return BALL_STATUS.NEITHER;  // not enough light causes
        // purple to be read as green
        boolean isGreen = false;
        boolean isPurple = false;
        double greenLowerH = 81;
        double greenLowerS = .27;
        double greenUpperH = 168.0;
        if(colorsHSV[0] > greenLowerH && colorsHSV[0] < greenUpperH && colorsHSV[1] > greenLowerS) {
            isGreen = true;
        }
        double purpleLowerH = 175.0;
        double purpleLowerS = .28;
        double purpleUpperH = 250.0;
        if(colorsHSV[0] > purpleLowerH && colorsHSV[0] < purpleUpperH && colorsHSV[1] > purpleLowerS) {
            isPurple = true;
        }

        panels.addData("isGreen?", isGreen);
        panels.addData("isPurple?", isPurple);

        if(isGreen && isPurple) {
            panels.addData("CONFLICT", "Read both purple and green balls");

            return BALL_STATUS.CONFLICT;
        }
        if(isGreen) return BALL_STATUS.GREEN;
        if(isPurple) return BALL_STATUS.PURPLE;
        return BALL_STATUS.NEITHER;
    }

    private BALL_STATUS combinedColorDecision(BALL_STATUS sensorOneColor, BALL_STATUS sensorTwoColor) {
        if(sensorOneColor.equals(sensorTwoColor)) return sensorOneColor;
        else if(sensorOneColor.equals(BALL_STATUS.NEITHER) || (sensorOneColor.equals(BALL_STATUS.CONFLICT) && !sensorTwoColor.equals(BALL_STATUS.NEITHER))) return sensorTwoColor;
        else if(sensorTwoColor.equals(BALL_STATUS.NEITHER) || sensorTwoColor.equals(BALL_STATUS.CONFLICT)) return sensorOneColor;
        else {
            panels.addData("CONFLICT", "sensor one detected " + sensorOneColor + " but sensor two detected " + sensorTwoColor);
            return BALL_STATUS.CONFLICT;
        }
    }

    private BALL_STATUS getColorInPositionOne(NormalizedColorSensor color1, NormalizedColorSensor color2) {
        float[] colors1 = new float[3]; // colors1[0] = H, index 1 is S, 2 is V
        float[] colors2 = new float[3];

        Color.colorToHSV(color1.getNormalizedColors().toColor(), colors1);
        Color.colorToHSV(color2.getNormalizedColors().toColor(), colors2);
        BALL_STATUS sensorOneDecision = checkBall(colors1);
        BALL_STATUS sensorTwoDecision = checkBall(colors2);
        BALL_STATUS ball_color = combinedColorDecision(sensorOneDecision, sensorTwoDecision);

        for (float value : colors1) {
            panels.addData("color1 colors", value);
        }
        panels.addData("colors1 color chosen", sensorOneDecision);
        panels.addData("","");
        for (float value : colors2) {
            panels.addData("color2 colors", value);
        }
        panels.addData("color2 color chosen", sensorTwoDecision);
        panels.addData("final color chosen", ball_color);

        return ball_color;
    }
}
