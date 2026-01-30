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
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

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
    private enum BALL_STATUS {GREEN, PURPLE, NEITHER, CONFLICT}
    BALL_STATUS positionOne = BALL_STATUS.PURPLE;
    BALL_STATUS positionTwo = BALL_STATUS.PURPLE;
    BALL_STATUS positionThree = BALL_STATUS.GREEN;
    private enum BALL_ORDER {GPP, PPG, PGP, NONE}
    BALL_STATUS targetOne, targetTwo, targetThree;
    BALL_STATUS[] targetColors;
    boolean isSpinning = false;
    int pos = 0;
    boolean useMotif = true;
    BALL_ORDER ballOrder;

    public void initPaths(Follower follower) {
        Pose startPose = new Pose(56.37113402061855, 7.257731958762889, Math.toRadians(270));
        Pose shootingPosition = new Pose(66.02061855670102, 78.30927835051546, Math.toRadians(315));
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
                                new Pose(56.16494845360825, 85.67010309278352),
                                new Pose(11.918, 84.124)
                        )
                ).setConstantHeadingInterpolation(Math.toRadians(180))
                .build();

        return1 = follower.pathBuilder().addPath(
                        new BezierLine(
                                new Pose(11.918, 84.124),
                                shootingPosition
                        )
                ).setLinearHeadingInterpolation(Math.toRadians(0), shootingPosition.getHeading())
                .build();

        leave = follower.pathBuilder().addPath(
                        new BezierLine(
                                shootingPosition,
                                new Pose(52.505, 70.010)
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
            } else {
                useMotif = false;
            }
            panels.addData("target 1", targetOne);
            panels.addData("target 2", targetTwo);
            panels.addData("target 3", targetThree);
            panels.update(telemetry);
        }
    }

    @Override
    public void start() {
        telemetry.setAutoClear(false);
        panels.getWrapper().setAutoClear(false);
    }

    @Override
    public void loop() {
        follower.update();
        if (!isSpinning && (Math.abs(pos - hw.indexerMotor.getCurrentPosition()) <= 20
                || Math.abs(pos - hw.indexerMotor.getCurrentPosition()) >= 200)) {
            hw.indexerMotor.setVelocity(0);
        }
        switch (state) {
            case 0:
                hw.intakeMotor.setPower(1);
                follower.followPath(score1, true);
                state = 1;
                break;
            case 1:
                if(!follower.isBusy()) {
                    hw.outtake(860);
                    pathTimer.resetTimer();
                    state = 2;
                }
                break;
            case 2:
                if (pathTimer.getElapsedTimeSeconds() > 3) {
                    outtakeTimer.resetTimer();
                    int targetOnePos = searchBallColor(targetOne);
                    if (targetOnePos != 0) shootFromPosition(targetOnePos);
                    pathTimer.resetTimer();
                    state = 3;
                }
                break;
            case 3:
                if (pathTimer.getElapsedTimeSeconds() > 2) {
                    outtakeTimer.resetTimer();
                    int targetTwoPos = searchBallColor(targetTwo);
                    if (targetTwoPos != 0) shootFromPosition(targetTwoPos);
                    pathTimer.resetTimer();
                    state = 4;
                }
                break;
            case 4:
                if (pathTimer.getElapsedTimeSeconds() > 2) {
                    outtakeTimer.resetTimer();
                    int targetThreePos = searchBallColor(targetThree);
                    if (targetThreePos != 0) shootFromPosition(targetThreePos);
                    pathTimer.resetTimer();
                    state = 5;
                }
                break;
            case 5:
                if (pathTimer.getElapsedTimeSeconds() > 3) {
                    hw.indexerMotor.setVelocity(1000);
                    follower.followPath(balls1);
                    pathTimer.resetTimer();
                    state = 6;
                }
                break;
            case 6:
                if (pathTimer.getElapsedTimeSeconds() > 1 && !follower.isBusy()) {
                    follower.followPath(return1, true);
                    pathTimer.resetTimer();
                    state = 61;
                }
                break;
            case 61:
                if(pathTimer.getElapsedTimeSeconds() > 1) {
                    getBallsInPosition();
                    pathTimer.resetTimer();
                    state = 7;
                }
                break;
            case 7:
                if (!follower.isBusy() && pathTimer.getElapsedTimeSeconds() > 2) {
                    outtakeTimer.resetTimer();
                    int targetOnePos = searchBallColor(targetOne);
                    if (targetOnePos != 0) shootFromPosition(targetOnePos);
                    pathTimer.resetTimer();
                    state = 8;
                }
                break;
            case 8:
                if (pathTimer.getElapsedTimeSeconds() > 4) {
                    outtakeTimer.resetTimer();
                    int targetTwoPos = searchBallColor(targetTwo);
                    if (targetTwoPos != 0) shootFromPosition(targetTwoPos);
                    pathTimer.resetTimer();
                    state = 9;
                }
                break;
            case 10:
                if (pathTimer.getElapsedTimeSeconds() > 2) {
                    outtakeTimer.resetTimer();
                    int targetThreePos = searchBallColor(targetThree);
                    if (targetThreePos != 0) shootFromPosition(targetThreePos);
                    pathTimer.resetTimer();
                    state = 99;
                }
                break;
            case 99:
                follower.followPath(leave);
                state = 100;
                break;
        }

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
                if(outtakeTimer.getElapsedTimeSeconds() > 1)
                    shootAndWait(31, () -> indexerToIntake(1), 99);
                break;
            case 2:
                shootAndWait(2, () -> indexerToIntake(1), 21);
                break;
            case 21:
                if(outtakeTimer.getElapsedTimeSeconds() > 1)
                    shootAndWait(21, () -> indexerToOuttake(1), 22);
                break;
            case 22:
                if(outtakeTimer.getElapsedTimeSeconds() > 1)
                    shootAndWait(22, () -> indexerToIntake(1), 99);
                break;
            case 1:
                shootAndWait(1, () -> indexerToIntake(1), 11);
                break;
            case 11:
                if(outtakeTimer.getElapsedTimeSeconds() > 1)
                    shootAndWait(11, () -> indexerToIntake(1), 12);
                break;
            case 12:
                if(outtakeTimer.getElapsedTimeSeconds() > 1)
                    shootAndWait(12, () -> indexerToOuttake(1), 13);
                break;
            case 13:
                if(outtakeTimer.getElapsedTimeSeconds() > 1)
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
            panels.addData("RUN", initialCase);
            panels.update(telemetry);
            isSpinning = true;
            outtakeTimer.resetTimer();
            initialShot.run();
        }
        if (Math.abs(pos - hw.indexerMotor.getCurrentPosition()) <= 20
                || Math.abs(pos - hw.indexerMotor.getCurrentPosition()) >= 200) {
            hw.indexerMotor.setVelocity(0);
            isSpinning = false;
//            panels.addData("run", "moving on to " + nextCase + " from " + initialCase);
////            panels.addData("case", initialCase);
////            panels.addData("next case", nextCase);
////            panels.addData("pos", pos);
////            panels.addData("curr", hw.indexerMotor.getCurrentPosition());
//            panels.addData("first", positionOne);
//            panels.addData("second", positionTwo);
//            panels.addData("third", positionThree);
//            panels.update(telemetry);
            shootFromPosition(nextCase); // 3_1 is like 3.1, the next step for position 3
        } else {
            shootFromPosition(initialCase);
        }
    }


    private void indexerToOuttake(int rotations) {
        pos = hw.velocityIndexerDown();
        for(int i = 0; i < rotations; i++) {
            positionThree = positionOne;
            positionOne = positionTwo;
            positionTwo = BALL_STATUS.NEITHER;  // position two neither because it gets launched
        }
    }

    private void indexerToIntake(int rotations) {
        pos = hw.velocityIndexerUp();
        BALL_STATUS temp;
        for(int i = 0; i < rotations; i++) {
            temp = positionThree;
            positionThree = positionTwo;
            positionTwo = positionOne;
            positionOne = temp;
        }
    }

    private void getBallsInPosition() {
        while(!positionOne.equals(BALL_STATUS.GREEN)) {
            hw.indexerMotor.setVelocity(200);
            positionOne = getColorInPositionOne(hw.color1, hw.color2);
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
