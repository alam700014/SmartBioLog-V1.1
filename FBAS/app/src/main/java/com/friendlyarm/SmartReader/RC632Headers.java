package com.friendlyarm.SmartReader;

/**
 * Created by fortuna on 22/5/17.
 */
public class RC632Headers {


//    #define page0  		0x00
//            #define cmnd		0x01
//            #define fifo		0x02
//            #define pstat		0x03
//            #define fifo_len   	0x04
//            #define sstat	    0x05
//            #define intr_en		0x06
//            #define intr_rq		0x07
//            #define cntrl		0x09
//            #define err_fl		0x0a
//            #define coll_pos	0x0b
//            #define tmr_val		0x0c
//            #define crc_res_lsb	0x0d
//            #define crc_res_msb	0x0e
//            #define bit_frm		0x0f
//            #define tx_cntrl	0x11
//            #define ant_condc	0x12
//            #define mod_wdth	0x15
//            #define rx_cntrl1	0x19
//            #define dcdr_cntrl	0x1a
//            #define bit_phs		0x1b
//            #define rx_thrhld	0x1c
//            #define rx_cntrl2	0x1e
//            #define clk_qcntrl	0x1f
//            #define rx_wait		0x21
//            #define chn_redun	0x22
//            #define crc_pre_lsb	0x23
//            #define crc_pre_msb	0x24
//            #define mf_out		0x26
//            #define fifo_lvl	0x29
//            #define tmr_clk		0x2a
//            #define tmr_cntrl	0x2b
//            #define tmr_reld	0x2c
//            #define irq_config	0x2d
//            #define test_d_sig	0x3d


    //-------------rc632 registers----------------//

    public static byte page0 = 0x00;
    public static byte cmnd = 0x01;
    public static byte fifo = 0x02;
    public static byte pstat = 0x03;
    public static byte fifo_len = 0x04;
    public static byte sstat = 0x05;
    public static byte intr_en = 0x06;
    public static byte intr_rq = 0x07;
    public static byte cntrl = 0x09;
    public static byte err_fl = 0x0a;
    public static byte coll_pos = 0x0b;
    public static byte tmr_val = 0x0c;
    public static byte crc_res_lsb = 0x0d;
    public static byte crc_res_msb = 0x0e;
    public static byte bit_frm = 0x0f;
    public static byte tx_cntrl = 0x11;
    public static byte ant_condc = 0x12;
    public static byte mod_wdth = 0x15;
    public static byte rx_cntrl1 = 0x19;
    public static byte dcdr_cntrl = 0x1a;
    public static byte bit_phs = 0x1b;
    public static byte rx_thrhld = 0x1c;
    public static byte rx_cntrl2 = 0x1e;
    public static byte clk_qcntrl = 0x1f;
    public static byte rx_wait = 0x21;
    public static byte chn_redun = 0x22;
    public static byte crc_pre_lsb = 0x23;
    public static byte crc_pre_msb = 0x24;
    public static byte mf_out = 0x26;
    public static byte fifo_lvl = 0x29;
    public static byte tmr_clk = 0x2a;
    public static byte tmr_cntrl = 0x2b;
    public static byte tmr_reld = 0x2c;
    public static byte irq_config = 0x2d;
    public static byte test_d_sig = 0x3d;
    boolean smart_present_f ;
}
