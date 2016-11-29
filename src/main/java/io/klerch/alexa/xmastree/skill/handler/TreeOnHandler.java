package io.klerch.alexa.xmastree.skill.handler;

import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.model.AlexaOutputSlot;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.schema.type.AlexaOutputFormat;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.xmastree.skill.SkillConfig;
import io.klerch.alexa.xmastree.skill.model.TreeState;

@AlexaIntentListener(customIntents = "TreeOn")
public class TreeOnHandler extends AbstractIntentHandler {
    @Override
    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        final AWSDynamoStateHandler dynamoHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());
        final TreeState treeState = dynamoHandler.readModel(TreeState.class).orElse(new TreeState(input.getLocale(), TreeState.MODE.ON));

        // set state on
        treeState.setState(TreeState.MODE.ON);

        // send state of tree to AWS IoT thing shadow
        sendIotHook(input, treeState);

        return AlexaOutput.tell("SayTreeOn")
                .putState(treeState.withHandler(dynamoHandler))
                .putSlot(new AlexaOutputSlot("mp3", SkillConfig.getMp3IntroUrl()).formatAs(AlexaOutputFormat.AUDIO))
                .build();
    }
}
