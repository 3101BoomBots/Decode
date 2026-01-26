package org.firstinspires.ftc.teamcode;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.List;


@Autonomous(name = "pedroAutoMotifRed")
public class pedroAutoMotifRed extends OpMode {
    PathChain score1, leave;
    Follower follower;
    Timer pathTimer;
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
    boolean useMotif = true;
    boolean purples = false;
    int remaining = 3;
    BALL_ORDER ballOrder;


    @Override
    public void init() {
        Pose startPose = new Pose(88.14432989690722, 7.051546391752584, Math.toRadians(270));
        pathTimer = new Timer();
        panels = PanelsTelemetry.INSTANCE.getTelemetry();
        follower = Constants.createFollower(hardwareMap);
        hw = Hardware.getInstance(this);
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);
        limelight.start();

        hw.pedroInit(hardwareMap);

        score1 = follower
                .pathBuilder()
                .addPath(
                        new BezierLine(startPose, new Pose(76.82474226804123, 77.93814432989691))
                )
                .setLinearHeadingInterpolation(Math.toRadians(270), Math.toRadians(225))
                .build();
        leave = follower.pathBuilder().addPath(
                new BezierLine(new Pose(76.82474226804123, 77.93814432989691,  Math.toRadians(225)),
                        new Pose(81.4639175257732, 60.68041237113401,  Math.toRadians(225)))
                ).setLinearHeadingInterpolation(Math.toRadians(225), Math.toRadians(225)).build();
        follower.setStartingPose(startPose);
        state = 0;
    }

    @Override
    public void init_loop() {
        LLResult result = limelight.getLatestResult();
        if (result.isValid()) {
            // Access general information
            List<LLResultTypes.FiducialResult> fiducialResults = result.getFiducialResults();
            for (LLResultTypes.FiducialResult fr : fiducialResults) {
                panels.debug("Fiducial", "ID: %d, Family: %s, X: %.2f, Y: %.2f", fr.getFiducialId(), fr.getFamily(), fr.getTargetXDegrees(), fr.getTargetYDegrees());
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
    public void loop() {
        follower.update();
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
                if(pathTimer.getElapsedTimeSeconds() > 5 && !follower.isBusy()) {  // is busy might not work

                    if(targetOne.equals(BALL_STATUS.GREEN)) {
                        hw.indexerDown(1);
                        while(hw.indexerMotor.isBusy()) {follower.update();}
                        hw.indexerUp(2);
                        while(hw.indexerMotor.isBusy()) {follower.update();}
                        purples = true;
                        rotateIndexerToIntake(1);
                        positionTwo = BALL_STATUS.NEITHER;
                        remaining = 2;
                    } else {
                        hw.indexerUp(1);
                        while(hw.indexerMotor.isBusy()) {follower.update();}
                        hw.indexerDown(1);
                        while(hw.indexerMotor.isBusy()) {follower.update();}
                        hw.indexerUp(2);
                        while(hw.indexerMotor.isBusy()) {follower.update();}
                    }
                    pathTimer.resetTimer();
                    state = 31;
                }
                break;
            case 31:
                if(pathTimer.getElapsedTimeSeconds() > 2) {  // is busy might not work

                    if (targetTwo.equals(BALL_STATUS.GREEN)) {
                        hw.indexerDown(1);
                        while (hw.indexerMotor.isBusy()) {
                            follower.update();
                        }
                        hw.indexerUp(2);
                        while (hw.indexerMotor.isBusy()) {
                            follower.update();
                        }
                        purples = true;
                        remaining = 1;
                    } else {
                        hw.indexerUp(1);
                        while (hw.indexerMotor.isBusy()) {
                            follower.update();
                        }
                        hw.indexerDown(1);
                        while (hw.indexerMotor.isBusy()) {
                            follower.update();
                        }
                        hw.indexerUp(2);
                        while (hw.indexerMotor.isBusy()) {
                            follower.update();
                        }
                    }
                    pathTimer.resetTimer();
                    state = 33;
                }
                break;
            case 33:
                if(pathTimer.getElapsedTimeSeconds() > 2) {  // is busy might not work

                    for (int i = 0; i < remaining; i++) {
                        while (hw.indexerMotor.isBusy()) {
                            follower.update();
                        }
                        hw.indexerDown(1);
                        while (hw.indexerMotor.isBusy()) {
                            follower.update();
                        }
                        hw.indexerUp(2);
                        while (hw.indexerMotor.isBusy()) {
                            follower.update();
                        }
                    }
                    hw.outtake(0);
                    state = 3;
                }
                break;
            case 3:
                follower.followPath(leave);
                state = 4;
                break;
        }

        panels.addData("path state", state);
        panels.addData("path timer", pathTimer.getElapsedTimeSeconds());
        panels.addData("velocityLeft", hw.outtakeMotorLeft.getVelocity());
        panels.addData("velocityRight", hw.outtakeMotorRight.getVelocity());
//        panels.addData("motif", ballOrder);
//        panels.addData("colors", targetColors);
//        telemetry.addData("x", follower.getPose().getX());
//        telemetry.addData("y", follower.getPose().getY());
//        telemetry.addData("heading", follower.getPose().getHeading());
        panels.update(telemetry);
    }

    public void rotateIndexerToIntake(int rotations) {
//        hw.indexerMotor.setTargetPosition(indexerMotor.getTargetPosition() - (int)(turns*0.333*INDEXER_RESOLUTION));
        BALL_STATUS temp;
        for(int i = 0; i < rotations; i++) {
            temp = positionThree;
            positionThree = positionTwo;
            positionTwo = positionOne;
            positionOne = temp;
        }
    }
}
