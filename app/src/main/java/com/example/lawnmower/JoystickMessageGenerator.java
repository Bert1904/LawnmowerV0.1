package com.example.lawnmower;

public class JoystickMessageGenerator {

    /**
     * Generates messages for the joystick with protobuf. For more detailed Information on the
     * protobuf messages, view the wiki.
     * @param X x value of the joystick from -1 to 1, from left to right.
     * @param Y y value of the joystick from -1 to 1, from top to bottom
     * @return the builded protobuf message.
     */
    public static AppControlsProtos.AppControls buildMessage(double X, double Y) {
        AppControlsProtos.AppControls.JoyStick joystick = AppControlsProtos.AppControls.JoyStick.newBuilder().setX(X).setY(Y).build();
        return AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.NO_CMD).setJoy(joystick).build();
    }
}

