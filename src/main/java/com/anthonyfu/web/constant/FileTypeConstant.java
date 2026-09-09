package com.anthonyfu.web.constant;

import java.util.ArrayList;
import java.util.List;

public class FileTypeConstant {

    // FAB 檔案條件
    public static final String FAB_FAZ = ".faz";
    public static final String FAB_OUT_PPT = "_out.ppt";
    public static final String FAB_OUT_PPTX = "_out.pptx";

    // CAD 檔案條件
    public static final String CAD = "-cad";

    // ODB 檔案條件
    public static final String ODB = "-odb";

    // PANEL 檔案條件
    public static final String PANEL = "-panel";

    // SMD 檔案條件
    public static final String SMD = "-smd";

    // SPEC 檔案條件
    public static final String SPEC = "-spc";

    // FAB
    public static final List<String> FAB_LIST = new ArrayList<String>() {{
        add(FAB_FAZ);
        add(FAB_OUT_PPT);
        add(FAB_OUT_PPTX);
    }};

    // CAD
    public static final List<String> CAD_LIST = new ArrayList<String>() {{
        add(CAD);
    }};

    // ODB
    public static final List<String> ODB_LIST = new ArrayList<String>() {{
        add(ODB);
    }};

    // PANEL
    public static final List<String> PANEL_LIST = new ArrayList<String>() {{
        add(PANEL);
    }};

    // SMD
    public static final List<String> SMD_LIST = new ArrayList<String>() {{
        add(SMD);
    }};

    // SPEC
    public static final List<String> SPEC_LIST = new ArrayList<String>() {{
        add(SPEC);
    }};

    // GERBER
    public static final List<String> GERBER_LIST = new ArrayList<String>() {{
        // 沒有特殊條件時，預設歸類為 GERBER
    }};

    private FileTypeConstant() {
    }
}
