package guru.springframework.ssm.msscssm.config;

import guru.springframework.ssm.msscssm.domain.PaymentEvent;
import guru.springframework.ssm.msscssm.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;

import java.util.EnumSet;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates() // Set up state machine possible states
                .initial(PaymentState.NEW) // Set initial state machine state
                .states(EnumSet.allOf(PaymentState.class)) // Set list of possible states
                .end(PaymentState.AUTH) // Set AUTH as a termination state
                .end(PaymentState.PRE_AUTH_ERROR) // Set error state as a termination state
                .end(PaymentState.AUTH_ERROR); // Set error state as a termination state
    }
}
