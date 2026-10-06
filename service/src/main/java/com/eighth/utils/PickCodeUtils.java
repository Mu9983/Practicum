package com.eighth.utils;

import com.eighth.Shelf;
import com.eighth.exception.BusinessException;

import java.util.Random;
import java.util.function.Function;

public class PickCodeUtils {

    private static final Random random = new Random();
    private static final int MAX_TRY = 50;
    private static final int CODE_RANGE = Shelf.SLOTS_PER_SHELF;

    /**
     * 单纯生成一个6位数字取件码（不做查重）
     * @return 6位字符串，带前导零，如 "001234"
     */
    public static String generateSixDigitCode() {
        int num = random.nextInt(CODE_RANGE);
        return String.format("%06d", num);
    }

    /**
     * 生成唯一6位取件码
     * @param existsFunc 查重函数：传入code，true=已存在，false=不存在
     * @return 唯一6位取件码
     * @throws BusinessException 重试耗尽，取件码池满
     */
    public static String generateUniquePickCode(Function<String,Boolean> existsFunc){
        int count = 0;
        String code;
        do {
            code = generateSixDigitCode();
            count++;
            if(count > MAX_TRY){
                throw new BusinessException("取件码池已耗尽，无法生成新取件码");
            }
        }while (existsFunc.apply(code));
        return code;
    }

}
