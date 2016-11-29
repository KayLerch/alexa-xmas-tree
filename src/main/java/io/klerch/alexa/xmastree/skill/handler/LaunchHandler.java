package io.klerch.alexa.xmastree.skill.handler;

import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.model.AlexaOutputSlot;
import io.klerch.alexa.tellask.schema.AlexaLaunchHandler;
import io.klerch.alexa.tellask.schema.annotation.AlexaLaunchListener;
import io.klerch.alexa.tellask.schema.type.AlexaOutputFormat;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.xmastree.skill.SkillConfig;
import io.klerch.alexa.xmastree.skill.model.TreeState;

import static io.klerch.alexa.xmastree.skill.handler.AbstractIntentHandler.sendIotHook;

@AlexaLaunchListener
public class LaunchHandler implements AlexaLaunchHandler {

    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        final AWSDynamoStateHandler dynamoHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());
        final TreeState treeState = dynamoHandler.readModel(TreeState.class).orElse(new TreeState(input.getLocale(), TreeState.MODE.ON));

        // set state on
        treeState.setState(TreeState.MODE.ON);

        // send state of tree to AWS IoT thing shadow
        sendIotHook(input, treeState);

        return AlexaOutput.ask("SayWelcome")
                .putState(treeState.withHandler(dynamoHandler))
                .putSlot(new AlexaOutputSlot("mp3", SkillConfig.getMp3IntroUrl()).formatAs(AlexaOutputFormat.AUDIO))
                .withReprompt(true)
                .build();
    }

    public AlexaOutput handleError(final AlexaRequestHandlerException exception) {
        return AlexaOutput.tell("SaySorry").build();
    }
}
