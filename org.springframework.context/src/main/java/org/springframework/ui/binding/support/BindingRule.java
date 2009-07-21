package org.springframework.ui.binding.support;

import org.springframework.ui.binding.config.Condition;
import org.springframework.ui.format.Formatter;

public interface BindingRule {
	
	Formatter<?> getFormatter();
	
	Formatter<?> getKeyFormatter();

	Formatter<?> getElementFormatter();
	
	Condition getEditableCondition();
	
	Condition getEnabledCondition();
	
	Condition getVisibleCondition();
	
}