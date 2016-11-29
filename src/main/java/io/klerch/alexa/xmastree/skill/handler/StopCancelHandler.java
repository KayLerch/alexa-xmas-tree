package io.klerch.alexa.xmastree.skill.handler;

import io.klerch.alexa.state.handler.AWSDynamoStateHandler;
import io.klerch.alexa.state.utils.AlexaStateException;
import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.schema.type.AlexaIntentType;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;
import io.klerch.alexa.xmastree.skill.model.TreeState;

@AlexaIntentListener(builtInIntents = {AlexaIntentType.INTENT_CANCEL, AlexaIntentType.INTENT_STOP})
public class StopCancelHandler extends AbstractIntentHandler {
    @Override
    public AlexaOutput handleRequest(final AlexaInput input) throws AlexaRequestHandlerException, AlexaStateException {
        final AWSDynamoStateHandler dynamoHandler = new AWSDynamoStateHandler(input.getSessionStateHandler().getSession());
        final TreeState treeState = dynamoHandler.readModel(TreeState.class).orElse(new TreeState(input.getLocale(), TreeState.MODE.ON));

        // set state stop
        treeState.setState(TreeState.MODE.STOP);

        // send state of tree to AWS IoT thing shadow
        sendIotHook(input, treeState);

        return AlexaOutput.tell("SayOk").build();
    }
}
