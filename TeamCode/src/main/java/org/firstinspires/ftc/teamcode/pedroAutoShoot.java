package org.firstinspires.ftc.teamcode;

import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.bylazar.telemetry.PanelsTelemetry;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;


@Autonomous(name = "pedroAutoBlue")
public class pedroAutoShoot extends OpMode {
    PathChain score1, leave;
    Follower follower;
    Timer pathTimer;
    TelemetryManager panels;
    Hardware hw;
    int state = -1;

    @Override
    public void init() {
        Pose startPose = new Pose(56, 8, Math.toRadians(270));
        pathTimer = new Timer();
        panels = PanelsTelemetry.INSTANCE.getTelemetry();
        follower = Constants.createFollower(hardwareMap);
        hw = Hardware.getInstance(this);
        hw.pedroInit(hardwareMap);

        score1 = follower
                .pathBuilder()
                .addPath(
                    new BezierLine(startPose, new Pose(68.28865979381443, 74.04123711340205))
                )
                .setLinearHeadingInterpolation(Math.toRadians(270), Math.toRadians(315))
                .build();
        leave = follower.pathBuilder().addPath(
                new BezierLine(new Pose(68.28865979381443, 74.04123711340205, 315),
                        new Pose(48.06185567010309, 74.59793814432989, 315))
        ).setLinearHeadingInterpolation(Math.toRadians(315), Math.toRadians(315)).build();
        follower.setStartingPose(startPose);
        state = 0;
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
                    hw.outtake(880);
                    pathTimer.resetTimer();
                    state = 2;
                }
                break;
            case 2:
                if(hw.outtakeMotorRight.getVelocity() >= 860 && pathTimer.getElapsedTimeSeconds() < 6) {  // is busy might not work
                    for(int i = 0; i < 3; i ++) {
                        hw.indexerDown(1);
                        while(hw.indexerMotor.isBusy()) {follower.update();}
                        hw.indexerUp(2);
                        while(hw.indexerMotor.isBusy()) {follower.update();}
                    }
                    hw.outtakeMotorRight.setVelocity(0);
                    state = 3;
                }
                break;
            case 3:
                follower.followPath(leave);
                state = 4;
                break;
        }

        panels.addData("path state", state);
        panels.addData("velocityLeft", hw.outtakeMotorLeft.getVelocity());
        panels.addData("velocityRight", hw.outtakeMotorRight.getVelocity());
//        telemetry.addData("x", follower.getPose().getX());
//        telemetry.addData("y", follower.getPose().getY());
//        telemetry.addData("heading", follower.getPose().getHeading());
        panels.update(telemetry);
    }
}
