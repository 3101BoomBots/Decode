package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.Arrays;
import java.util.List;


@Autonomous(name = "pedroAutoMotifBlue")
public class pedroAutoMotifBlue extends OpMode {
    PathChain score1, balls1, return1, leave;
    Follower follower;
    Timer pathTimer;
    Timer outtakeTimer;
    TelemetryManager panels;
    Hardware hw;
    Limelight3A limelight;
    int state = -1;
    BALL_STATUS positionOne = BALL_STATUS.PURPLE;
    BALL_STATUS positionTwo = BALL_STATUS.PURPLE;
    BALL_STATUS positionThree = BALL_STATUS.GREEN;
    private enum BALL_ORDER {GPP, PPG, PGP, NONE}
    BALL_STATUS targetOne, targetTwo, targetThree;
    BALL_STATUS[] targetColors;
    boolean isFirst = false;
    int pos = 0;
    boolean useMotif = true;
    BALL_ORDER ballOrder;

    public void initPaths(Follower follower) {
        Pose startPose = new Pose(54.32989690721649, 7.443298969072167, Math.toRadians(270));
        Pose shootingPosition = new Pose(65.09278350515464, 78.86597938144331, Math.toRadians(315));
        follower.setStartingPose(startPose);
        score1 = follower.pathBuilder().addPath(
                        new BezierLine(
                                startPose,
                                shootingPosition
                        )
                ).setLinearHeadingInterpolation(startPose.getHeading(), shootingPosition.getHeading())
                .build();

        balls1 = follower.pathBuilder().addPath(
                        new BezierCurve(
                                shootingPosition,
                                new Pose(13.773195876288682, 84.12371134020619),
                                new Pose(53.1958762886598, 85.48453608247424)
                        )
                ).setConstantHeadingInterpolation(Math.toRadians(180))
                .build();

        return1 = follower.pathBuilder().addPath(
                        new BezierLine(
                                new Pose(65.00000000000001, 78.70103092783506),
                                shootingPosition
                        )
                ).setLinearHeadingInterpolation(Math.toRadians(0), shootingPosition.getHeading())
                .build();

        leave = follower.pathBuilder().addPath(
                        new BezierLine(
                                shootingPosition,
                                new Pose(52.319587628865975, 68.340206185567)
                        )
                ).setConstantHeadingInterpolation(shootingPosition.getHeading())
                .build();
    }


    @Override
    public void init() {
        pathTimer = new Timer();
        outtakeTimer = new Timer();
        panels = PanelsTelemetry.INSTANCE.getTelemetry();

        follower = Constants.createFollower(hardwareMap);
        hw = Hardware.getInstance(this);
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);
        limelight.start();

        hw.pedroInit(hardwareMap);

        initPaths(follower);
        state = 0;
    }

    @Override
    public void init_loop() {
        LLResult result = limelight.getLatestResult();
        if (result.isValid()) {
            // Access general information
            List<LLResultTypes.FiducialResult> fiducialResults = result.getFiducialResults();
            for (LLResultTypes.FiducialResult fr : fiducialResults) {
//                panels.debug("Fiducial", "ID: %d, Family: %s, X: %.2f, Y: %.2f", fr.getFiducialId(), fr.getFamily(), fr.getTargetXDegrees(), fr.getTargetYDegrees());
//                panels.update(telemetry);
                if(fr.getFiducialId() == 21) ballOrder = BALL_ORDER.GPP;
                else if(fr.getFiducialId() == 22) ballOrder = BALL_ORDER.PGP;
                else if(fr.getFiducialId() == 23) ballOrder = BALL_ORDER.PPG;
//                else ballOrder = BALL_ORDER.NONE;
            }
            if(ballOrder != null && !ballOrder.equals(BALL_ORDER.NONE)) {
                if(ballOrder.equals(BALL_ORDER.GPP)) {
                    targetOne = BALL_STATUS.GREEN;
                    targetTwo = BALL_STATUS.PURPLE;
                    targetThree = BALL_STATUS.PURPLE;
                }
                else if(ballOrder.equals(BALL_ORDER.PGP)) {
                    targetOne = BALL_STATUS.PURPLE;
                    targetTwo = BALL_STATUS.GREEN;
                    targetThree = BALL_STATUS.PURPLE;
                }
                else if(ballOrder.equals(BALL_ORDER.PPG)) {
                    targetOne = BALL_STATUS.PURPLE;
                    targetTwo = BALL_STATUS.PURPLE;
                    targetThree = BALL_STATUS.GREEN;
                }
                targetColors = new BALL_STATUS[] {targetOne, targetTwo, targetThree};
                panels.addData("target 1", targetOne);
                panels.addData("target 2", targetTwo);
                panels.addData("target 3", targetThree);
                panels.update(telemetry);
            } else {
                useMotif = false;
            }
        }
    }

    @Override
    public void start() {
//        telemetry.setAutoClear(false);
//        panels.getWrapper().setAutoClear(false);
    }

    @Override
    public void loop() {
        follower.update();
        switch (state) {
            case 0:
                hw.intakeMotor.setPower(1);
                follower.followPath(score1, true);
                state = 1;
                break;
            case 98:  // debug case
//                hw.intakeMotor.setPower(1);
                state = 1;
            case 1:
                if(!follower.isBusy()) {
                    hw.outtake(860);
                    pathTimer.resetTimer();
                    state = 2;
                }
                break;
            case 2:
                if (pathTimer.getElapsedTimeSeconds() > 1.2) {
                    outtakeTimer.resetTimer();
//                    int targetOnePos = searchBallColor(targetOne);
                    if(targetOne.equals(BALL_STATUS.PURPLE)) shootFromPosition(2);
                    else shootFromPosition(3);
                    resetPosition(hw);
                    pathTimer.resetTimer();
                    state = 3;
                }
                break;
            case 3:
                if (pathTimer.getElapsedTimeSeconds() > 0.8) {
                    outtakeTimer.resetTimer();
                    if(targetTwo.equals(BALL_STATUS.PURPLE)) shootFromPosition(2);
                    else shootFromPosition(1);
                    resetPosition(hw);
                    pathTimer.resetTimer();
                    state = 4;
                }
                break;
            case 4:
                if (pathTimer.getElapsedTimeSeconds() > 0.8) {
                    outtakeTimer.resetTimer();
                    if(ballOrder.equals(BALL_ORDER.PGP)) shootFromPosition(3);
                    else shootFromPosition(2);
                    resetPosition(hw);
                    pathTimer.resetTimer();
                    state = 5;   // debug case
                }
                break;
            case 5:
//                if (pathTimer.getElapsedTimeSeconds() > 0.2) {
                hw.indexerMotor.setVelocity(400);
                follower.followPath(balls1, 0.6, true);
                pathTimer.resetTimer();
                resetPosition(hw);
                state = 51;
//                }
                break;
            case 51:
                for(int i = 0; i < 6; i++) {
                    while (pathTimer.getElapsedTimeSeconds() < 0.15) {
                        hw.indexerMotor.setVelocity(400);
                        follower.update();
                    }
                    pathTimer.resetTimer();
                    while (pathTimer.getElapsedTimeSeconds() < 0.15) {
                        hw.indexerMotor.setVelocity(0);
                        follower.update();
                    }
                }
                pathTimer.resetTimer();
                state = 6;
                break;
            case 52:
                if(pathTimer.getElapsedTimeSeconds() > 0.3) {
                    hw.indexerMotor.setVelocity(400);
                    pathTimer.resetTimer();
                    state = 6;
                }
                break;
            case 6:
                if (pathTimer.getElapsedTimeSeconds() > 1  && !follower.isBusy()) { // wait at end 1/2 second
                    follower.followPath(return1, true);
//                    Timer ballResetTimer = new Timer();
//                    getBallsInPosition(ballResetTimer);
                    pathTimer.resetTimer();
                    state = 7;
                }
                break;
            case 7:
                if (!follower.isBusy()) {
                    outtakeTimer.resetTimer();
                    isFirst = true;
                    shootFromPosition(2);
                    resetPosition(hw);
                    isFirst = false;
                    pathTimer.resetTimer();
                    state = 8;
                }
                break;
            case 8:
                if (pathTimer.getElapsedTimeSeconds() > 0.7) {
                    outtakeTimer.resetTimer();
                    shootFromPosition(2);
                    resetPosition(hw);
                    pathTimer.resetTimer();
                    state = 9;
                }
                break;
            case 9:
                if (pathTimer.getElapsedTimeSeconds() > 0.7) {
                    outtakeTimer.resetTimer();
                    shootFromPosition(2);
                    resetPosition(hw);
                    pathTimer.resetTimer();
                    state = 10;
                }
                break;
            case 10:
                if (pathTimer.getElapsedTimeSeconds() > 0.3) {
                    outtakeTimer.resetTimer();
                    shootFromPosition(2);
                    resetPosition(hw);
                    pathTimer.resetTimer();
                    state = 99;
                }
                break;
            case 99:
                hw.intakeMotor.setPower(0);
                hw.indexerMotor.setPower(0);
                hw.outtake(0);
                follower.followPath(leave);
                state = 100;
                break;
        }
        Hardware.startingPose = follower.getPose();

//        panels.addData("path state", state);
//        panels.addData("path timer", pathTimer.getElapsedTimeSeconds());
//        panels.addData("velocityLeft", hw.outtakeMotorLeft.getVelocity());
//        panels.addData("velocityRight", hw.outtakeMotorRight.getVelocity());
//        panels.addData("motif", ballOrder);
//        panels.addData("colors", targetColors);
//        telemetry.addData("x", follower.getPose().getX());
//        telemetry.addData("y", follower.getPose().getY());
//        telemetry.addData("heading", follower.getPose().getHeading());
//        panels.update(telemetry);
    }

    private void resetPosition(Hardware hw) {
        int closestPos = closestPosition(hw.indexerMotor.getCurrentPosition(), (int)Hardware.INDEXER_RESOLUTION);
//        panels.addData("curr and pos and closest", " curr" + hw.indexerMotor.getCurrentPosition() +
//                " " + pos + " " + closestPos);
//        panels.update(telemetry);
        hw.indexerMotor.setTargetPosition(closestPos);
        if(!hw.indexerMotor.getMode().equals(DcMotor.RunMode.RUN_TO_POSITION)) hw.indexerRun();
        hw.indexerMotor.setPower(1);
        while(hw.indexerMotor.isBusy()) {follower.update();}
        hw.indexerMotor.setPower(0);
        hw.indexerVelocity();
    }

    private int closestPosition(int currPos, int indexerResolution) {
        int multiplier = currPos / indexerResolution;
//        panels.addData("multiplier", multiplier);
//        panels.update(telemetry);
        int[] positions = generatePositions(multiplier);
        Integer[] differences = new Integer[]{
                Math.abs(currPos - positions[0]),
                Math.abs(currPos - positions[1]),
                Math.abs(currPos - positions[2])};
        List<Integer> differenceToPos = Arrays.asList(differences);
        int minDiff = Math.min(Math.min(differenceToPos.get(0), differenceToPos.get(1)), differenceToPos.get(2));
        int indexOfMin = differenceToPos.indexOf(minDiff);
        return positions[indexOfMin];
    }

    private int[] generatePositions(int multiplier) {
        return new int[]{(int) (Hardware.INDEXER_RESOLUTION * multiplier),
                (int) (0.333 * Hardware.INDEXER_RESOLUTION + multiplier * Hardware.INDEXER_RESOLUTION),
                (int) (0.666 * Hardware.INDEXER_RESOLUTION + multiplier * Hardware.INDEXER_RESOLUTION)};
    }

    private void shootFromPosition(int position) {
        // gpp = 322
        // ppg = 222
        // pgp = 213
        // 3 : 1 down 1 up
        // 2 : 1 up 1 down 1 up
        // 1 : 2 up 1 down
        final double OUTTAKE_DELAY = 0.1;
        switch (position) {
            case 3:
                while(outtakeTimer.getElapsedTimeSeconds() < OUTTAKE_DELAY) {follower.update();}
                shootAndWait(3, () -> indexerToOuttake(true), 31);
                break;
            case 31:
                while(outtakeTimer.getElapsedTimeSeconds() < OUTTAKE_DELAY) {follower.update();}
                shootAndWait(31, this::indexerToIntake, 99);
                break;
            case 2:
                while(outtakeTimer.getElapsedTimeSeconds() < OUTTAKE_DELAY) {follower.update();}
                shootAndWait(2, this::indexerToIntake, 21);
                break;
            case 21:
                while(outtakeTimer.getElapsedTimeSeconds() < OUTTAKE_DELAY){follower.update();}
                shootAndWait(21, () -> indexerToOuttake(isFirst), 22);
                break;
            case 22:
                while(outtakeTimer.getElapsedTimeSeconds() < OUTTAKE_DELAY){follower.update();}
                shootAndWait(22, this::indexerToIntake, 99);
                break;
            case 1:
                while(outtakeTimer.getElapsedTimeSeconds() < OUTTAKE_DELAY) {follower.update();}
                shootAndWait(1, this::indexerToIntake, 11);
                break;
            case 11:
                while(outtakeTimer.getElapsedTimeSeconds() < OUTTAKE_DELAY){follower.update();}
                shootAndWait(11, this::indexerToIntake, 12);
                break;
            case 12:
                while(outtakeTimer.getElapsedTimeSeconds() < OUTTAKE_DELAY){follower.update();}
                shootAndWait(12, () -> indexerToOuttake(false), 99);
                break;
            case 99:  // designated base case
                hw.indexerMotor.setVelocity(0);
                break;
            default:
                throw new IllegalArgumentException("Target position is not 1, 2, or 3");
        }
    }

    private void shootAndWait(int initialCase, Runnable initialShot, int nextCase) {
        panels.addData("RUN", initialCase);
        panels.update(telemetry);
        outtakeTimer.resetTimer();
        initialShot.run();

        while (!(Math.abs(pos - hw.indexerMotor.getCurrentPosition()) <= 55
                || Math.abs(pos - hw.indexerMotor.getCurrentPosition()) >= Hardware.INDEXER_RESOLUTION)) {
            follower.update();

            panels.addData("pos", pos);
            panels.addData("velocity", hw.indexerMotor.getVelocity());
            panels.addData("curr", hw.indexerMotor.getCurrentPosition());
            panels.update(telemetry);
        }
//        panels.addData("run", "moving from " + initialCase + " to " + nextCase);
//        panels.addData("second", positionTwo);
//        panels.update(telemetry);

        hw.indexerMotor.setVelocity(0);
        outtakeTimer.resetTimer();
        shootFromPosition(nextCase); // 3_1 is like 3.1, the next step for position 3
    }


    private void indexerToOuttake(boolean extraHardFirstBall) {
        pos = hw.velocityIndexerDown(extraHardFirstBall);
        positionThree = positionOne;
        positionOne = positionTwo;
        positionTwo = BALL_STATUS.NEITHER;  // position two neither because it gets launched
    }

    private void indexerToIntake() {
        pos = hw.velocityIndexerUp();
        BALL_STATUS temp;
        temp = positionThree;
        positionThree = positionTwo;
        positionTwo = positionOne;
        positionOne = temp;
    }

    private void getBallsInPosition(Timer timeoutTimer) {
        while(!positionOne.equals(BALL_STATUS.GREEN) || timeoutTimer.getElapsedTimeSeconds() > 3) {
            hw.indexerMotor.setVelocity(100);
            positionOne = getColorInPositionOne(hw.color1, hw.color2);
            follower.update();
        }
        positionOne = BALL_STATUS.GREEN;
        positionTwo = BALL_STATUS.PURPLE;
        positionThree = BALL_STATUS.PURPLE;
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
        panels.update(telemetry);

        return ball_color;
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
}
