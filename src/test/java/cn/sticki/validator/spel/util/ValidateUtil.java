package cn.sticki.validator.spel.util;

import cn.sticki.validator.spel.ExceptionField;
import cn.sticki.validator.spel.VerifyObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

/**
 * 测试验证工具类
 *
 * @author 阿杆
 * @version 1.0
 * @since 2024/6/13
 */
@Slf4j
public class ValidateUtil {

	@SuppressWarnings("resource")
	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	/**
	 * 参数校验
	 * <p>
	 * 调用此方法会触发 javax.validation.constraints.* 的校验，类似于使用 @Valid 注解
	 *
	 * @return 校验结果，如果校验通过则返回空列表
	 */
	public static <T> Set<ConstraintViolation<T>> validate(T obj) {
		return validator.validate(obj);
	}

	/**
	 * 验证约束结果是否符合预期
	 */
	public static boolean checkConstraintResult(List<VerifyObject> verifyObjectList) {
		int failCount = 0;

		for (VerifyObject verifyObject : verifyObjectList) {
			Object object = verifyObject.getObject();
			Set<ExceptionField> exceptionFields = verifyObject.getExceptionFields();

			String className = object.getClass().getSimpleName();

			MDC.put("className", className);
			if (object instanceof ID) {
				MDC.put("id", String.valueOf(((ID) object).getId()));
			}
			log.info("Start checking object: {}", object);

			// 执行约束校验
			Set<ConstraintViolation<Object>> validate = ValidateUtil.validate(object);

			// 检查结果是否符合预期
			for (ExceptionField exceptionField : exceptionFields) {
				String fieldName = exceptionField.getName();
				MDC.put("fieldName", fieldName);
				String message = exceptionField.getMessage();

				log.info("Expected exception information: {}", message == null ? "ignore" : message);

				boolean fieldMatch = false, find = false;

				for (ConstraintViolation<Object> violation : validate) {
					String violationFieldName = violation.getPropertyPath().toString();
					// 找到对应的字段
					if (violationFieldName.equals(fieldName)) {
						find = true;
						log.info("Real exception information: {}", violation.getMessage());

						// 异常信息不同时验证失败（没填写异常信息则不校验异常信息）
						if (message != null && !message.equals(violation.getMessage())) {
							log.error("Failed");
						} else {
							fieldMatch = true;
							log.info("Passed");
						}

						validate.remove(violation);
						break;
					}
				}

				if (!find) {
					// 多余的字段
					log.error("Excess field");
				}
				if (!fieldMatch) {
					failCount++;
				}
			}
			MDC.remove("fieldName");

			// 被忽略的字段
			for (ConstraintViolation<Object> violation : validate) {
				log.error("Field [{}] is ignored", violation.getPropertyPath().toString());
				failCount++;
			}

			log.info("Verification end, number of failures: {}", failCount);
			log.info("------------------------------------------------------------------------");
			MDC.clear();
		}
		return failCount == 0;
	}

}
