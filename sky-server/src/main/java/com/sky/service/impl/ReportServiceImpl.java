package com.sky.service.impl;

import com.aliyuncs.http.HttpResponse;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jing Beier
 * @version 1.0
 * @date 2026/5/3
 * @function 统计数据业务层
 */

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间区域内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //计算dateList,用于存放begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while(!begin.equals(end)){
            //计算范围内的每一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();

        //从数据库查询我们每天的营业额
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额数据，营业额是指，状态为"已完成"的订单金额统计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);

            //若不存在为null，我们设置数据为0.0
            turnover = turnover == null? 0.0 : turnover;
            turnoverList.add(turnover);

        }

        String dateListString = StringUtils.join(dateList, ",");
        String turnoverListString = StringUtils.join(turnoverList, ",");

        return TurnoverReportVO.builder()
                .dateList(dateListString)
                .turnoverList(turnoverListString).build();
    }

    /**
     * 统计指定时间区间内的用户数数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getuserStatistics(LocalDate begin, LocalDate end) {

        //计算dateList,用于存放begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while(!begin.equals(end)){
            //计算范围内的每一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //存放每天的总用户数量
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end", endTime);
            //总的用户数量
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin", beginTime);
            //每天新增的用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        String dateListString = StringUtils.join(dateList, ",");
        String totalUserString = StringUtils.join(totalUserList, ",");
        String newUserString = StringUtils.join(newUserList, ",");

        return UserReportVO.builder()
                .dateList(dateListString)
                .totalUserList(totalUserString)
                .newUserList(newUserString)
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getordersStatistics(LocalDate begin, LocalDate end) {

        //计算dateList,用于存放begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while(!begin.equals(end)){
            //计算范围内的每一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> totalOrderList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();


        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询每天的订单总数
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            //查询每天的有效订单数
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            totalOrderList.add(orderCount);
            validOrderList.add(validOrderCount);
        }

        //计算时间区间内的订单总数
        Integer totalOrderCount = totalOrderList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderList.stream().reduce(Integer::sum).get();


        Double orderCompletetionRate = 0.0;
        if(totalOrderCount != 0) {
            //计算订单完成率
            orderCompletetionRate = validOrderCount.doubleValue() / totalOrderCount * 100;
        }
        String dateListString = StringUtils.join(dateList, ",");
        String totalOrderListString = StringUtils.join(totalOrderList, ",");
        String validOrderListString = StringUtils.join(validOrderList, ",");

        return OrderReportVO.builder()
                .dateList(dateListString)
                .orderCountList(totalOrderListString)
                .validOrderCountList(validOrderListString)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletetionRate)
                .build();
    }

    /**
     * 统计指定时间区间内的销售排名前十
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalrsTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //拿到前十的菜品名和销量
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop(beginTime, endTime);

        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 将方法提出
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinData(HttpServletResponse response){
        //1.查询数据库,获取营业数据,查询最近30天数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);

        //查询概览数据
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);

        //2.使用POI将营业数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间" + dateBegin + "至" + dateEnd);

            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO bd = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(bd.getTurnover());
                row.getCell(3).setCellValue(bd.getValidOrderCount());
                row.getCell(4).setCellValue(bd.getOrderCompletionRate());
                row.getCell(5).setCellValue(bd.getUnitPrice());
                row.getCell(6).setCellValue(bd.getNewUsers());
            }

            //3.通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            out.close();
            excel.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
