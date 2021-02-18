package com.example.lawnmower;
public class ButtonMessageGenerator {
    /*
    * Build Lawnmower command messages from protobuff file
    */
    public static AppControlsProtos.AppControls buildMessage(int i) {
        AppControlsProtos.AppControls btn;
        switch(i) {
            case (1):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.START).build();
                break;
            case(2):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.STOP).build();
                break;
            case(3):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.PAUSE).build();
                break;
            case(4):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.HOME).build();
                break;
            case(5):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.BEGIN_TRACKING).build();
                break;
            case(6):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.FINISH_TRACKING).build();
                break;
            case(7):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.REBOOT).build();
                break;
            case(8):
                btn = AppControlsProtos.AppControls.newBuilder().setCmd(AppControlsProtos.AppControls.Command.SHUTDOWN).build();
                break;
            default:
                btn = null;
                break;
        }
        return btn;
    }
}