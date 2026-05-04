package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @author Jing Beier
 * @version 1.0
 * @date 2026/5/3
 * @function
 */

public interface ReportService {

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);


    /**
     * 统计指定时间区间内的用户数数据
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getuserStatistics(LocalDate begin, LocalDate end);

    /**
     * 统计指定时间区间内的订单数数据
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getordersStatistics(LocalDate begin, LocalDate end);

    /**
     * 统计指定时间区间内的销售排名前十
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalrsTop10(LocalDate begin, LocalDate end);
}
