package guru.springframework.ssm.msscssm.config;

import guru.springframework.ssm.msscssm.config.actions.*;
import guru.springframework.ssm.msscssm.domain.PaymentEvent;
import guru.springframework.ssm.msscssm.domain.PaymentState;
import guru.springframework.ssm.msscssm.config.guards.PaymentIdGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@RequiredArgsConstructor
@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    // Autowired by matching field name to bean name
    private final Guard<PaymentState, PaymentEvent> paymentIdGuard;
    private final Action<PaymentState, PaymentEvent> preAuthAction;
    private final Action<PaymentState, PaymentEvent> authAction;
    private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;
    private final Action<PaymentState, PaymentEvent> preAuthDeclinedAction;
    private final Action<PaymentState, PaymentEvent> authApprovedAction;
    private final Action<PaymentState, PaymentEvent> authDeclinedAction;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates() // Set up state machine possible states
                .initial(PaymentState.NEW) // Set initial state machine state
                .states(EnumSet.allOf(PaymentState.class)) // Set list of possible states
                .end(PaymentState.AUTH) // Set AUTH as a termination state
                .end(PaymentState.PRE_AUTH_ERROR) // Set error state as a termination state
                .end(PaymentState.AUTH_ERROR); // Set error state as a termination state
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                .withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE) // when in NEW state, when PRE_AUTH event occurs, stay in NEW state
                    .action(preAuthAction).guard(paymentIdGuard) // and trigger preAuthAction if id is non-null
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED) // when in NEW state, when PRE_AUTH_APPROVED event occurs, move to PRE_AUTH state
                    .action(preAuthApprovedAction) // and trigger preAuthApprovedAction (notification)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED) // when in NEW state,  when PRE_AUTH_DECLINED event occurs, move to PRE_AUTH_ERROR state
                    .action(preAuthDeclinedAction) // and trigger preAuthDeclinedAction (notification)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE) // when in PRE_AUTH state, when AUTHORIZE event occurs, stay in PRE_AUTH state
                    .action(authAction) // and trigger authAction
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED) // when in PRE_AUTH state, when AUTH_APPROVED event occurs, move to AUTH state
                    .action(authApprovedAction)  // and trigger authApprovedAction (notification)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED) // when in PRE_AUTH state, when AUTH_DECLINED event occurs, move to AUTH_ERROR state
                    .action(authDeclinedAction); // and trigger authDeclinedAction (notification)
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>(){
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stateChanged(from: %s, to: %s", from, to));
            }
        };

        config.withConfiguration().listener(adapter);
    }

//    public Guard<PaymentState, PaymentEvent> paymentIdGuard() {
//        return stateContext -> {
//            return stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
//        };
//    }

//    public Action<PaymentState, PaymentEvent> preAuthAction() {
//        return stateContext -> {
//            System.out.println("PreAuth was called!!!");
//            if (new Random().nextInt(10) < 8) {
//                System.out.println("Approved!");
//                stateContext.getStateMachine()
//                        .sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
//                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
//                                stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
//                        .build());
//            } else {
//                System.out.println("Declined! No Credit!!!!!!!!");
//                stateContext.getStateMachine()
//                        .sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
//                                .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
//                                        stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
//                                .build());
//            }
//        };
//    }

//    public Action<PaymentState, PaymentEvent> authAction() {
//        return stateContext -> {
//            System.out.println("Auth was called!!!");
//            if (new Random().nextInt(10) < 8) {
//                System.out.println("Auth Approved!");
//                stateContext.getStateMachine()
//                        .sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
//                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
//                                stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
//                        .build());
//            } else {
//                System.out.println("Auth Declined!");
//                stateContext.getStateMachine()
//                        .sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
//                                .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,
//                                        stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
//                                .build());
//            }
//        };
//    }

}
