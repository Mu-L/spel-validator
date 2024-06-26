package cn.sticki.validator.spel.exception;

import lombok.Getter;

import java.util.Set;

/**
 * 不支持的类型异常
 *
 * @author 阿杆
 * @version 1.0
 * @since 2024/5/3
 */
@Getter
public class SpelNotSupportedTypeException extends SpelValidException {

	private final Class<?> clazz;

	private final Set<Class<?>> supperType;

	public SpelNotSupportedTypeException(Class<?> clazz, Set<Class<?>> supperType) {
		super("Object class not supported, class: " + clazz.getName() + ", supperType: " + supperType.toString());
		this.clazz = clazz;
		this.supperType = supperType;
	}

}
