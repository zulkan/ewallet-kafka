package com.zulkan.ewallet.config;

import com.zulkan.ewallet.model.User;
import com.zulkan.ewallet.utils.Utils;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        return Utils.getUserFromContext().map(User::getUsername).or(() -> Optional.of("System"));
    }
}
