package cn.lanzp.jffw.model;

/**
 * 子午流注时辰数据模型
 * 12条时辰数据，经🦞🌿3号中医顾问审核通过
 */
public class MeridianData {
    private int id;              // 0-11
    private String shichen;      // 时辰名：子、丑、寅...
    private String timeRange;    // 现代时间：23:00-00:59
    private String meridian;     // 经络：足少阳胆经
    private String element;      // 五行：木
    private String acupoint;     // 推荐穴位：足窍阴、风池
    private String flowInfo;     // 流注说明
    private String healthTip;    // 养生建议

    public MeridianData() {}

    public MeridianData(int id, String shichen, String timeRange, String meridian,
                        String element, String acupoint, String flowInfo, String healthTip) {
        this.id = id;
        this.shichen = shichen;
        this.timeRange = timeRange;
        this.meridian = meridian;
        this.element = element;
        this.acupoint = acupoint;
        this.flowInfo = flowInfo;
        this.healthTip = healthTip;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getShichen() { return shichen; }
    public void setShichen(String shichen) { this.shichen = shichen; }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public String getMeridian() { return meridian; }
    public void setMeridian(String meridian) { this.meridian = meridian; }

    public String getElement() { return element; }
    public void setElement(String element) { this.element = element; }

    public String getAcupoint() { return acupoint; }
    public void setAcupoint(String acupoint) { this.acupoint = acupoint; }

    public String getFlowInfo() { return flowInfo; }
    public void setFlowInfo(String flowInfo) { this.flowInfo = flowInfo; }

    public String getHealthTip() { return healthTip; }
    public void setHealthTip(String healthTip) { this.healthTip = healthTip; }

    /**
     * 返回全部12条子午流注数据
     * 🦞🌿3号中医顾问审核通过 ✅
     */
    public static MeridianData[] getAllData() {
        return new MeridianData[]{
                new MeridianData(0, "子", "23:00-00:59", "足少阳胆经", "木",
                        "足窍阴、风池",
                        "胆为少阳春生之气，主决断。子时胆经气血最旺，胆汁推陈出新，是身体修复和再生的开始。",
                        "宜：熟睡养胆。子时前入睡，胆气方能生发，有利于骨髓造血和代谢排毒。忌：熬夜、吃夜宵。"),
                new MeridianData(1, "丑", "01:00-02:59", "足厥阴肝经", "木",
                        "太冲、行间",
                        "肝藏血，主疏泄。丑时肝经气血最旺，肝脏进行解毒和血液净化，是身体深层修复的关键时段。",
                        "宜：熟睡养肝血。深度睡眠中肝血归藏，全身血液得到滤清更新。忌：熬夜、饮酒、情绪波动。"),
                new MeridianData(2, "寅", "03:00-04:59", "手太阴肺经", "金",
                        "太渊、列缺",
                        "肺朝百脉，主治节。寅时肺经气血最旺，肺将气血输送全身，为新一天的生命活动做准备。",
                        "宜：深度睡眠。肺主一身之气，熟睡中气血重新分配全身。忌：此时起床剧烈运动，肺气易伤。"),
                new MeridianData(3, "卯", "05:00-06:59", "手阳明大肠经", "金",
                        "合谷、曲池",
                        "大肠主传导糟粕。卯时大肠经气血旺盛，是排便的最佳时机，身体清空肠道迎接新一天的饮食。",
                        "宜：喝杯温水唤醒肠胃，养成定时排便习惯。起床后轻度拉伸活动。忌：睡懒觉憋便，不吃早餐。"),
                new MeridianData(4, "辰", "07:00-08:59", "足阳明胃经", "土",
                        "足三里、天枢",
                        "胃主受纳腐熟水谷。辰时胃经气血最旺，胃的消化吸收能力最强，早餐的营养价值最高。",
                        "宜：吃一顿温热丰盛的早餐，细嚼慢咽。此时进食最容易消化吸收。忌：不吃早餐或吃寒凉生冷食物。"),
                new MeridianData(5, "巳", "09:00-10:59", "足太阴脾经", "土",
                        "太白、三阴交",
                        "脾主运化，升清降浊。巳时脾经气血旺盛，将早餐营养输送到全身，是人体能量供给的黄金时段。",
                        "宜：投入工作和学习，此时段大脑供血充足，工作效率最高。忌：久坐不动、暴饮暴食。"),
                new MeridianData(6, "午", "11:00-12:59", "手少阴心经", "火",
                        "神门、少海",
                        "心主血脉，藏神。午时心经气血最旺，心火旺盛，此时适合小憩让心神得以休息，为下午养精蓄锐。",
                        "宜：午餐后小憩15-30分钟，闭目养神可有效养护心气。忌：剧烈运动、情绪激动。"),
                new MeridianData(7, "未", "13:00-14:59", "手太阳小肠经", "火",
                        "后溪、腕骨",
                        "小肠主泌别清浊。未时小肠经气血旺盛，将午餐的营养精微吸收输布，同时将糟粕送入大肠。",
                        "宜：多喝水助代谢，此时小肠吸收功能旺盛，水分补充尤为重要。忌：憋尿、饭后马上躺下。"),
                new MeridianData(8, "申", "15:00-16:59", "足太阳膀胱经", "水",
                        "委中、昆仑",
                        "膀胱主气化行水。申时膀胱经气血最旺，是人体新陈代谢最快、排毒能力最强的时段。",
                        "宜：运动排毒最佳时段。适合跑步、打球等中等强度运动，大量饮水促进代谢。忌：久坐憋尿。"),
                new MeridianData(9, "酉", "17:00-18:59", "足少阴肾经", "水",
                        "太溪、涌泉",
                        "肾藏精，主生长发育。酉时肾经气血旺盛，肾脏开始贮藏精气，是养肾固本的最佳时段。",
                        "宜：宜静养，晚餐以清淡易消化为宜。可按摩涌泉穴固肾强身。忌：过度劳累、剧烈运动伤肾气。"),
                new MeridianData(10, "戌", "19:00-20:59", "手厥阴心包经", "火",
                        "内关、大陵",
                        "心包代心受邪，保护心脏。戌时心包经气血旺盛，是护心养心的最佳时机，心情趋于平和。",
                        "宜：散步轻运动，保持心情愉悦。适合与家人交流、听音乐、阅读。忌：剧烈运动、情绪过激。"),
                new MeridianData(11, "亥", "21:00-22:59", "手少阳三焦经", "火",
                        "外关、阳池",
                        "三焦主通调水道，运行元气。亥时三焦经气血旺盛，全身脏腑进入休整状态，为睡眠做准备。",
                        "宜：准备入睡，泡脚温通经络。放松身心，放下手机。忌：熬夜、剧烈运动、进食过饱。")
        };
    }
}
