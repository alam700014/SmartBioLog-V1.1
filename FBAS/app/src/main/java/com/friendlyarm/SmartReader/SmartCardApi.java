package com.friendlyarm.SmartReader;

/**
 * Created by fortuna on 22/5/17.
 */
public class SmartCardApi {


    static byte snr0 = 0x00;
    static byte snr1 = 0x00;
    static byte snr2 = 0x00;
    static byte snr3 = 0x00;
    static byte snr_chk = 0x00;

    static byte[] tmp_buff = new byte[8];

    byte intr_en = RC632Headers.intr_en;
    byte intr_rq = RC632Headers.intr_rq;
    byte cmnd = RC632Headers.cmnd;
    byte chn_redun = RC632Headers.chn_redun;
    byte cntrl = RC632Headers.cntrl;
    byte bit_frm = RC632Headers.bit_frm;
    byte tx_cntrl = RC632Headers.tx_cntrl;
    byte fifo = RC632Headers.fifo;
    byte fifo_len = RC632Headers.fifo_len;
    byte dcdr_cntrl = RC632Headers.dcdr_cntrl;
    byte err_fl = RC632Headers.err_fl;
    byte sstat = RC632Headers.sstat;

    // final static int SMART_ENENT_TIME = 50000;

    final static int SMART_ENENT_TIME = 20;
   // final static int SMART_ENENT_TIME = 10;

    int SM_REQ_ERR = 1;
    int SM_ANTCOL_ERR = 2;
    int SM_SEL_ERR = 3;
    int SM_LDKEY_ERR = 4;
    int SM_AUTH1_ERR = 5;
    int SM_AUTH2_ERR = 6;
    int SM_RD_ERR = 7;
    int SM_WR_ERR = 8;
    int SM_INC_DEC_ERR = 9;
    int SM_TRNSFR_ERR = 10;


//    unsigned char read_CSN(unsigned char rw_bit,unsigned char csn_blk, char * t_csn_buf)
//    {
//        char info_buf[16]="";
//
//        //unsigned long  j = 0;
//        //--------------------------------------------------------------------------//
//        //--------------------------------------------------------------------------//
//
//        smart_card_halt();
//        //--------------------------------------------------------------------------//
//        if(smart_card_request(info_buf))
//        {
//            display("smart_card_request => ");
//            lfcr();
//            return 1;
//        }
//
//
//        //--------------------------------------------------------------------------//
//        if(smart_card_anticollision())
//        {
//            display("smart_card_anticollision");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//        if(smart_card_select(info_buf))
//        {
//            display("smart_card_select");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//        //--------------------------------------------------------------------------//
//        //smart_card_format_key(stx_b);//====================
//        if(smart_card_load_key(stx_b))
//        {
//            display("smart_card_load_key");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//        if(smart_card_authourization_1(csn_blk,KEY_B))//===================
//        {
//            display("smart_card_authourization_1");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//        if(smart_card_authourization_2())
//        {
//            display("smart_card_authourization_2");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//
//        if(!rw_bit)
//        {
//
//            if(smart_card_read(csn_blk,t_csn_buf))
//            {
//                display("smart_card_read");
//                lfcr();
//                return 1;
//            }
//        }
//
//        else
//        {
//
//            if(smart_card_write(csn_blk,t_csn_buf))
//            {
//                display("smart_card_write");
//                lfcr();
//                return 1;
//            }
//
//        }
//
//        smart_card_halt();
//        //--------------------------------------------------------------------------//
//        //--------------------------------------------------------------------------//
//
//        return 0;
//
//
//
//    }


    RC632Api rc632Api = new RC632Api();

    public SmartCardApi(RC632Api rc632Api) {
        this.rc632Api = rc632Api;
    }

//    public byte read_CSN(byte rw_bit, byte csn_blk, byte[] t_csn_buf) {
//        byte[] info_buf = new byte[16];
//
//        //unsigned long  j = 0;
//        //--------------------------------------------------------------------------//
//        //--------------------------------------------------------------------------//
//
//        smart_card_halt();
//        //--------------------------------------------------------------------------//
//        if (smart_card_request(info_buf)) {
//            display("smart_card_request => ");
//            lfcr();
//            return 1;
//        }
//
//
//        //--------------------------------------------------------------------------//
//        if (smart_card_anticollision()) {
//            display("smart_card_anticollision");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//        if (smart_card_select(info_buf)) {
//            display("smart_card_select");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//        //--------------------------------------------------------------------------//
//        //smart_card_format_key(stx_b);//====================
//        if (smart_card_load_key(stx_b)) {
//            display("smart_card_load_key");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//        if (smart_card_authourization_1(csn_blk, KEY_B))//===================
//        {
//            display("smart_card_authourization_1");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//        if (smart_card_authourization_2()) {
//            display("smart_card_authourization_2");
//            lfcr();
//            return 1;
//        }
//        //--------------------------------------------------------------------------//
//
//        if (!rw_bit) {
//
//            if (smart_card_read(csn_blk, t_csn_buf)) {
//                display("smart_card_read");
//                lfcr();
//                return 1;
//            }
//        } else {
//
//            if (smart_card_write(csn_blk, t_csn_buf)) {
//                display("smart_card_write");
//                lfcr();
//                return 1;
//            }
//
//        }
//
//        smart_card_halt();
//        //--------------------------------------------------------------------------//
//        //--------------------------------------------------------------------------//
//
//        return 0;
//
//
//    }


//    unsigned char smart_card_get_info(char *buff)
//    {
//        unsigned char smart_card_error_no = 0;
//
//        smart_card_halt();
//
//        if(smart_card_request(buff))
//        {
//            smart_card_error_no = SM_REQ_ERR;
//        }
//        else
//        {
//            if(smart_card_anticollision())
//            {
//                smart_card_error_no = SM_ANTCOL_ERR;
//            }
//            else
//            {
//                if(smart_card_select(buff))
//                {
//                    smart_card_error_no = SM_SEL_ERR;
//                }
//            }
//        }
//        return smart_card_error_no;
//    }


    public int smart_card_get_info(byte[] charBuff) {

        int smart_card_error_no = 0;
        smart_card_halt();

        if (smart_card_request(charBuff) != 0) {
            smart_card_error_no = SM_REQ_ERR;
        } else {
            if (smart_card_anticollision() != 0) {
                smart_card_error_no = SM_ANTCOL_ERR;
            } else {
                if (smart_card_select(charBuff) != 0) {
                    smart_card_error_no = SM_SEL_ERR;
                } else {

                    //============================Extract First Four Bytes For CSN==========================//

                    byte[] csn = new byte[4];

                    for (int i = 0; i < 4; i++) {
                        csn[i] = charBuff[i];
                    }

                    //=====================================================================================//

                }
            }
        }
        return smart_card_error_no;
    }

    public String bytesToHex(byte[] data) {

        String strHexData = "";
        StringBuilder builder1 = new StringBuilder();
        for (byte b : data) {
            builder1.append(String.format("%02x", b));
        }
        strHexData = builder1.toString().toUpperCase();

        return strHexData;
    }


//    public static String bytesToHex(byte[] bytes) {
//        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
//        char[] hexChars = new char[bytes.length * 2];
//        int v;
//        for (int j = 0; j < bytes.length; j++) {
//            v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }

    private static String toASCII(int value) {
        int length = 4;
        StringBuilder builder = new StringBuilder(length);
        for (int i = length - 1; i >= 0; i--) {
            builder.append((char) ((value >> (8 * i)) & 0xFF));
        }
        return builder.toString();
    }

    //====================================Smart Card Halt==================================================//

    public void smart_card_halt() {


//       //-----------------------------------------------------//
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_flash_fifo();
//        tmp_buff[0] = 0x50;
//        tmp_buff[1] = 0x00;
//        rc632_fifo_write(2, tmp_buff);
//        rc632_byte_out(intr_en, 0x8c);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x1e);
//        //-----------------------------------------------------//
//        smart_card_get_event(SMART_ENENT_TIME);
//        //-----------------------------------------------------//
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_flash_fifo();
//        rc632_byte_out(cmnd, 0x00);
//        //-----------------------------------------------------//

        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632FlashFIFO();

        tmp_buff[0] = 0x50;
        tmp_buff[1] = 0x00;

        rc632Api.rc632FIFOWrite(2, tmp_buff);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x8c);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x1e);

        //  Log.d("TEST", "Smart Card Event Start");

        smart_card_get_event(SMART_ENENT_TIME);

        //  Log.d("TEST", "Smart Card Event Stop");
        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632FlashFIFO();
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
    }

    //=======================================Smart Card Request====================================================//

//    int error = 0;
//    //-----------------------------------------------------//
//    rc632_byte_out(chn_redun, 0x03);
//    rc632_clear_bit_mask(cntrl,0x08);
//    rc632_byte_out(bit_frm, 0x07);
//    rc632_set_bit_mask(tx_cntrl,0x03);
//    rc632_byte_out(intr_en, 0x3f);
//    rc632_byte_out(intr_rq, 0x3f);
//    rc632_byte_out(cmnd, 0x00);
//    rc632_flash_fifo();
//    rc632_byte_out(fifo, 0x52);
//    rc632_byte_out(intr_en, 0x88);
//    rc632_byte_out(intr_rq, 0x3f);
//    rc632_byte_out(cmnd, 0x1e);
//    //-----------------------------------------------------//
//    error = smart_card_get_event(SMART_ENENT_TIME);
//    //-----------------------------------------------------//
//    if(!error)
//    {
//        if(rc632_byte_in(fifo_len) == 0x02)
//        {	rc632_fifo_read(2, tmp_buff);	error = 0;	}
//        else
//        {	error = 1;	}
//    }
//    //-----------------------------------------------------//
//    rc632_byte_out(cmnd, 0x00);
//    //-----------------------------------------------------//
//    return error;
//    //-----------------------------------------------------//


    static byte[] stx_b = new byte[]{0x43, 0x50, 0x53, 0x20, 0x49, 0x44};    //key B
    static byte[] sec0_key = new byte[]{0x52, 0x45, 0x53, 0x53, 0x45, 0x43};


    //smart_card_format_key(sec0_key);
    //rc632Api.rc632FIFOWrite(12,key_buffer);//sm_key_buffer


    public int smart_card_request(byte[] buff) {

        int error = 0;


//        rc632_byte_out(chn_redun, 0x03);
//        rc632_clear_bit_mask(cntrl,0x08);
//        rc632_byte_out(bit_frm, 0x07);
//        rc632_set_bit_mask(tx_cntrl,0x03);
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x00);
//        rc632_flash_fifo();
//        rc632_byte_out(fifo, 0x52);
//        rc632_byte_out(intr_en, 0x88);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x1e);

        rc632Api.rc632ByteOut(chn_redun, (byte) 0x03);
        rc632Api.rs632ClearBitMask(cntrl, (byte) 0x08);
        rc632Api.rc632ByteOut(bit_frm, (byte) 0x07);
        rc632Api.rc632SetBitMask(tx_cntrl, (byte) 0x03);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        rc632Api.rc632FlashFIFO();
        rc632Api.rc632ByteOut(fifo, (byte) 0x52);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x88);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x1e);

//        try{
//
//            Thread.sleep(5000);
//        }catch(Exception e){
//
//        }


        //  Log.d("TEST", "IRQ:" + rc632Api.rc632IRQ());
        error = smart_card_get_event(SMART_ENENT_TIME);

//        byte val=rc632Api.rc632ByteIn((byte)0x0A);
//        Log.d("TEST","Error Flag:"+val);
//        String s1 = String.format("%8s", Integer.toBinaryString(val & 0xFF)).replace(' ', '0');
//        Log.d("TEST", "Write Byte Binary Format:" + s1); // 10000001

//        byte val=rc632Api.rc632ByteIn((byte) 0x1C);
//        Log.d("TEST", "Minimum Signal Strength:" + (val & 0xFF));
//
//        val=rc632Api.rc632ByteIn((byte) 0x21);
//        Log.d("TEST","RX Wait Register:"+val);
//
//        val=rc632Api.rc632ByteIn((byte)0x1E);
//        Log.d("TEST","Automatic Receive"+val);
//
//        val=rc632Api.rc632ByteIn((byte)0x03);
//        Log.d("TEST", "Primary Status Register" + val);

        // error=0;

        if (error == 0) {
            // Log.d("TEST", "XY.11 ");
            if (rc632Api.rc632ByteIn(fifo_len) == 0x02) {
                rc632Api.rc632FIFORead(2, tmp_buff);
                error = 0;
            } else {
                error = 1;
            }
        }
        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        //-----------------------------------------------------//
        return error;
        //-----------------------------------------------------//

    }

    //============================================Smart Card Anti Collision============================================//
//
//    int smart_card_anticollision(void)
//    {
//        int error = 0;
//        //-----------------------------------------------------//
//        rc632_byte_out(dcdr_cntrl, 0x28);
//        rc632_byte_out(chn_redun, 0x03);
//        rc632_clear_bit_mask(cntrl,0x08);
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x00);
//        rc632_flash_fifo();
//        tmp_buff[0] = 0x93;
//        tmp_buff[1] = 0x20;
//        rc632_fifo_write(2, tmp_buff);
//        rc632_byte_out(intr_en, 0x88);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x1e);
//        //-----------------------------------------------------//
//        error = smart_card_get_event(SMART_ENENT_TIME);
//        //-----------------------------------------------------//
//        if(!error)
//        {
//            if(rc632_byte_in(fifo_len) == 0x05)
//            {
//                rc632_fifo_read(5, tmp_buff);
//                snr0 = tmp_buff[0];
//                snr1 = tmp_buff[1];
//                snr2 = tmp_buff[2];
//                snr3 = tmp_buff[3];
//                snr_chk = tmp_buff[4];
//                error = 0;
//            }
//            else{	error = 1;	}
//        }
//        //-----------------------------------------------------//
//        rc632_clear_bit_mask(dcdr_cntrl, 0x20);
//        rc632_byte_out(cmnd, 0x00);
//        //-----------------------------------------------------//
//        return error;
//        //-----------------------------------------------------//
//    }


    int smart_card_anticollision() {
        int error = 0;
        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(dcdr_cntrl, (byte) 0x28);
        rc632Api.rc632ByteOut(chn_redun, (byte) 0x03);
        rc632Api.rs632ClearBitMask(cntrl, (byte) 0x08);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        rc632Api.rc632FlashFIFO();
        tmp_buff[0] = (byte) 0x93;
        tmp_buff[1] = (byte) 0x20;
        rc632Api.rc632FIFOWrite(2, tmp_buff);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x88);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x1e);
        //-----------------------------------------------------//
        error = smart_card_get_event(SMART_ENENT_TIME);
        //error=0;
        //-----------------------------------------------------//
        if (error == 0) {
            if (rc632Api.rc632ByteIn(fifo_len) == 0x05) {
                rc632Api.rc632FIFORead(5, tmp_buff);
                snr0 = tmp_buff[0];
                snr1 = tmp_buff[1];
                snr2 = tmp_buff[2];
                snr3 = tmp_buff[3];
                snr_chk = tmp_buff[4];
                error = 0;
            } else {
                error = 1;
            }
        }
        //-----------------------------------------------------//
        rc632Api.rs632ClearBitMask(dcdr_cntrl, (byte) 0x20);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        //-----------------------------------------------------//
        return error;
        //-----------------------------------------------------//
    }


    //==========================================Smart Card Select=================================================//
//
//    int smart_card_select(char *buff)
//    {
//        int error = 0;
//        //-----------------------------------------------------//
//        rc632_byte_out(chn_redun, 0x0f);
//        rc632_clear_bit_mask(cntrl,0x08);
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x00);
//        rc632_flash_fifo();
//        tmp_buff[0] = 0x93;
//        tmp_buff[1] = 0x70;
//        tmp_buff[2] = snr0;
//        tmp_buff[3] = snr1;
//        tmp_buff[4] = snr2;
//        tmp_buff[5] = snr3;
//        tmp_buff[6] = snr_chk;
//        rc632_fifo_write(7, tmp_buff);
//        rc632_byte_out(intr_en, 0x88);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x1e);
//        //-----------------------------------------------------//
//        error = smart_card_get_event(SMART_ENENT_TIME);
//        //-----------------------------------------------------//
//        if(!error)
//        {
//            if(rc632_byte_in(fifo_len) == 1)
//            {
//                buff[4] = rc632_byte_in(fifo);	//get ATS (card memory type)
//                buff[3] = snr0;
//                buff[2] = snr1;
//                buff[1] = snr2;
//                buff[0] = snr3;
//            }
//            else{	error = 1;	}
//        }
//        //-----------------------------------------------------//
//        rc632_byte_out(cmnd, 0x00);
//        //-----------------------------------------------------//
//        return error;
//        //-----------------------------------------------------//
//    }


    int smart_card_select(byte[] buff) {
        int error = 0;
        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(chn_redun, (byte) 0x0f);
        rc632Api.rs632ClearBitMask(cntrl, (byte) 0x08);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        rc632Api.rc632FlashFIFO();
        tmp_buff[0] = (byte) 0x93;
        tmp_buff[1] = (byte) 0x70;
        tmp_buff[2] = snr0;
        tmp_buff[3] = snr1;
        tmp_buff[4] = snr2;
        tmp_buff[5] = snr3;
        tmp_buff[6] = snr_chk;
        rc632Api.rc632FIFOWrite(7, tmp_buff);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x88);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x1e);
        //-----------------------------------------------------//
        error = smart_card_get_event(SMART_ENENT_TIME);
        // error=0;
        //-----------------------------------------------------//
        if (error == 0) {
            if (rc632Api.rc632ByteIn(fifo_len) == 1) {
                buff[4] = rc632Api.rc632ByteIn(fifo);    //get ATS (card memory type)
                buff[3] = snr0;
                buff[2] = snr1;
                buff[1] = snr2;
                buff[0] = snr3;
            } else {
                error = 1;
            }
        }
        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        //-----------------------------------------------------//
        return error;
        //-----------------------------------------------------//
    }


//    void smart_card_format_key(char *key)
//    {
//
//        unsigned char x = 0,y = 0,z = 0;
//
//        for(x = 0; x < 6; x++)
//        {
//            y = ((*key >> 4) & 0x0F);
//            z = (((~(*key)) & 0xF0) | y);
//            key_buffer[2 * x] = z;
//            y = (*key & 0x0F);
//            z =  ((~(*key << 4) & 0xF0) | y);
//            key_buffer[2 * x + 1] = z;key++;
//        }
//    }

    static byte[] key_buffer = new byte[12];

    public void smart_card_format_key(byte[] key) {
        int x = 0;
        byte y = 0;
        byte z = 0;

        for (x = 0; x < 6; x++) {
            y = (byte) ((key[x] >> 4) & 0x0F);
            z = (byte) (((~(key[x])) & 0xF0) | y);
            key_buffer[2 * x] = z;
            y = (byte) (key[x] & 0x0F);
            z = (byte) ((~(key[x] << 4) & 0xF0) | y);
            key_buffer[2 * x + 1] = z;
        }
    }

    int smart_card_get_event(int ev_val) {

//        int pinStatus;
//        pinStatus = rc632Api.rc632IRQ();
//        if (pinStatus != -1 && pinStatus != 1) {
//            return 1;
//        } else {
//            return 0;
//        }


        int pinStatus;
        while ((pinStatus = rc632Api.rc632IRQ()) != -1) {
            if (pinStatus != 1) {
                // Log.d("TEST","Event Counter:"+ev_val);
                if (--ev_val == 0) {
                    return 1;
                }
            } else {
                return 0;
            }
        }
        return 1;

//        while(!irq_632())
//        {
//            if(!ev_val){return 1;}
//            else{ev_val--;}
//        }
    }

//    int smart_card_load_key(char *key_buff)
//    {
//        int error = 0;
//        //-----------------------------------------------------//
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x00);
//        rc632_flash_fifo();
//        smart_card_format_key(key_buff);
//        rc632_fifo_write(12, key_buffer);//sm_key_buffer
//        rc632_byte_out(intr_en, 0x84);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x19);
//        //-----------------------------------------------------//
//        error = smart_card_get_event(SMART_ENENT_TIME);
//        //-----------------------------------------------------//
//        if(!error)
//        {
//            if((rc632_byte_in(err_fl) & 0x40) == 0x40){	error = 1;	}
//            else{	error = 0;	}
//        }
//        //-----------------------------------------------------//
//        rc632_byte_out(cmnd, 0x00);
//        //-----------------------------------------------------//
//        return error;
//        //-----------------------------------------------------//
//    }

    public int smartCardLoadKey(byte[] keyBuff) {

        int error = 0;

        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        rc632Api.rc632FlashFIFO();
        smart_card_format_key(keyBuff);
        rc632Api.rc632FIFOWrite(12, key_buffer);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x84);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x19);

        error = smart_card_get_event(SMART_ENENT_TIME);
        // error=0;

        if (error == 0) {
            if ((rc632Api.rc632ByteIn(err_fl) & 0x40) == 0x40) {
                error = 1;
            } else {
                error = 0;
            }

        }
        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        //-----------------------------------------------------//
        return error;
        //-----------------------------------------------------//
    }

//
//    int smart_card_authourization_1(unsigned char blk_no, unsigned int keya_flag)
//    {
//        int error = 0;
//        //-----------------------------------------------------//
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x00);
//        rc632_flash_fifo();
//        //-----------------------------------------------------//
//        if(keya_flag)
//        { 	tmp_buff[0] = 0x60;	}
//        else
//        { 	tmp_buff[0] = 0x61; }
//        //-----------------------------------------------------//
//        tmp_buff[1] = blk_no;
//        tmp_buff[2] = snr0;
//        tmp_buff[3] = snr1;
//        tmp_buff[4] = snr2;
//        tmp_buff[5] = snr3;
//        rc632_fifo_write(6, tmp_buff);
//        rc632_byte_out(intr_en, 0x84);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x0c);
//        //-----------------------------------------------------//
//        error = smart_card_get_event(SMART_ENENT_TIME);
//        //-----------------------------------------------------//
//        if(!error)
//        {
//            if((rc632_byte_in(sstat) & 0x07) == 0x07)
//            {	error = 1;	}
//            else{	error = 0;	}
//        }
//        //-----------------------------------------------------//
//        rc632_byte_out(cmnd, 0x00);
//        //-----------------------------------------------------//
//        return error;
//        //-----------------------------------------------------//
//    }

    public int smartCardAuthorization_1(byte blockNo, int keya_flag) {

        int error = 0;

        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        rc632Api.rc632FlashFIFO();

        if (keya_flag == 1) {
            tmp_buff[0] = 0x60;
        } else {
            tmp_buff[0] = 0x61;
        }

        tmp_buff[1] = blockNo;
        tmp_buff[2] = snr0;
        tmp_buff[3] = snr1;
        tmp_buff[4] = snr2;
        tmp_buff[5] = snr3;

        rc632Api.rc632FIFOWrite(6, tmp_buff);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x84);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x0c);

        error = smart_card_get_event(SMART_ENENT_TIME);
        //  error=0;

        if (error == 0) {
            if ((rc632Api.rc632ByteIn(sstat) & 0x07) == 0x07) {
                error = 1;
            } else {
                error = 0;
            }
        }

        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        //-----------------------------------------------------//
        return error;
        //-----------------------------------------------------//
    }
//
//    int smart_card_authourization_2(void)
//    {
//        int error = 0;
//        //-----------------------------------------------------//
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x00);
//        rc632_flash_fifo();
//
//        rc632_byte_out(intr_en, 0x84);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x14);
//        //-----------------------------------------------------//
//        error = smart_card_get_event(SMART_ENENT_TIME);
//        //-----------------------------------------------------//
//        if(!error)
//        {
//            if((rc632_byte_in(cntrl) & 0x08) != 0x08)
//            {  	error = 1;	}
//            else
//            {	error = 0;	}
//        }
//        //-----------------------------------------------------//
//        rc632_byte_out(cmnd, 0x00);
//        //-----------------------------------------------------//
//        return error;
//        //-----------------------------------------------------//
//    }

    public int smartCardAuthorization_2() {

        int error = 0;

        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        rc632Api.rc632FlashFIFO();
        rc632Api.rc632ByteOut(intr_en, (byte) 0x84);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x14);

        error = smart_card_get_event(SMART_ENENT_TIME);

        //  error=0;
        if (error == 0) {
            if ((rc632Api.rc632ByteIn(cntrl) & 0x08) != 0x08) {
                error = 1;
            } else {
                error = 0;
            }
        }

        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        //-----------------------------------------------------//
        return error;
        //-----------------------------------------------------//


    }

//    int smart_card_read(unsigned char blk_no,char *buff )
//    {
//        int error = 0;
//        //-----------------------------------------------------//
//        rc632_byte_out(chn_redun, 0x0f);
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x00);
//        rc632_flash_fifo();
//        tmp_buff[0] = 0x30;
//        tmp_buff[1] = blk_no;
//        rc632_fifo_write(2, tmp_buff);
//        rc632_byte_out(intr_en, 0x8c);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x1e);
//        //-----------------------------------------------------//
//        error = smart_card_get_event(SMART_ENENT_TIME);
//        //-----------------------------------------------------//
//        if(!error)
//        {
//            if(rc632_byte_in(fifo_len) == 0x10)
//            {	error = 0;rc632_fifo_read(16, buff);	}
//            else
//            {	error = 1;	}
//        }
//        //-----------------------------------------------------//
//        rc632_byte_out(cmnd, 0x00);
//        //-----------------------------------------------------//
//        return error;
//        //-----------------------------------------------------//
//    }

    public int smartCardRead(byte blockNo, byte[] readBuff) {

        int error = 0;

        rc632Api.rc632ByteOut(chn_redun, (byte) 0x0f);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        rc632Api.rc632FlashFIFO();
        tmp_buff[0] = 0x30;
        tmp_buff[1] = blockNo;
        rc632Api.rc632FIFOWrite(2, tmp_buff);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x8c);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x1e);

        error = smart_card_get_event(SMART_ENENT_TIME);
        //error=0;

        if (error == 0) {

            if (rc632Api.rc632ByteIn(fifo_len) == 0x10) {
                error = 0;
                rc632Api.rc632FIFORead(16, readBuff);
            } else {
                error = 1;
            }

        }

        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        //-----------------------------------------------------//
        return error;
        //-----------------------------------------------------//

    }
//
//    int smart_card_write(unsigned char blk_no,char *buff)
//    {
//        int error = 0;
//        //-----------------------------------------------------//
//        rc632_byte_out(chn_redun, 0x0f);
//        rc632_byte_out(intr_en, 0x3f);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x00);
//        rc632_flash_fifo();
//        tmp_buff[0] = 0xa0;
//        tmp_buff[1] = blk_no;
//        rc632_fifo_write(2, tmp_buff);
//        rc632_byte_out(intr_en, 0x84);
//        rc632_byte_out(intr_rq, 0x3f);
//        rc632_byte_out(cmnd, 0x1e);
//        //-----------------------------------------------------//
//        error = smart_card_get_event(SMART_ENENT_TIME);
//        //-----------------------------------------------------//
//        if(!error)
//        {
//            if( (rc632_byte_in(fifo) & 0x0f) == 0x0a)
//            {	error = 0;	}
//            else
//
//            {	error = 1;	}
//        }
//        //-----------------------------------------------------//
//        rc632_byte_out(cmnd, 0x00);
//        //-----------------------------------------------------//
//        if(!error)
//        {
//
//            rc632_byte_out(intr_en, 0x3f);
//            rc632_byte_out(intr_rq, 0x3f);
//            rc632_byte_out(cmnd, 0x00);
//            rc632_flash_fifo();
//
//            rc632_fifo_write(16, buff);
//
//            rc632_byte_out(intr_en, 0x84);
//            rc632_byte_out(intr_rq, 0x3f);
//            rc632_byte_out(cmnd, 0x1e);
//            //-----------------------------------------------------//
//            error = smart_card_get_event(SMART_ENENT_TIME);
//            //-----------------------------------------------------//
//            if(!error)
//            {
//                if( (rc632_byte_in(fifo) & 0x0f) == 0x0a)
//                {	error = 0;	}
//                else
//                {	error = 1;	}
//            }
//        }
//        //-----------------------------------------------------//
//        rc632_byte_out(cmnd, 0x00);
//        //-----------------------------------------------------//
//        return error;
//        //-----------------------------------------------------//
//    }

    public int smartCardWrite(byte blockNo, byte[] writeBuff) {

        int error = 0;

        rc632Api.rc632ByteOut(chn_redun, (byte) 0x0f);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x3f);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        rc632Api.rc632FlashFIFO();
        tmp_buff[0] = (byte) 0xa0;
        tmp_buff[1] = blockNo;
        rc632Api.rc632FIFOWrite(2, tmp_buff);
        rc632Api.rc632ByteOut(intr_en, (byte) 0x84);
        rc632Api.rc632ByteOut(intr_rq, (byte) 0x3f);
        rc632Api.rc632ByteOut(cmnd, (byte) 0x1e);

        error = smart_card_get_event(SMART_ENENT_TIME);

        if (error == 0) {
            if ((rc632Api.rc632ByteIn(fifo) & 0x0f) == 0x0a) {
                error = 0;
            } else

            {
                error = 1;
            }
        }
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);

        if (error == 0) {

            rc632Api.rc632ByteOut(intr_en, (byte)0x3f);
            rc632Api.rc632ByteOut(intr_rq,(byte) 0x3f);
            rc632Api.rc632ByteOut(cmnd,(byte) 0x00);
            rc632Api.rc632FlashFIFO();

            rc632Api.rc632FIFOWrite(16, writeBuff);

            rc632Api.rc632ByteOut(intr_en, (byte)0x84);
            rc632Api.rc632ByteOut(intr_rq,(byte) 0x3f);
            rc632Api.rc632ByteOut(cmnd, (byte)0x1e);
            //-----------------------------------------------------//
            error = smart_card_get_event(SMART_ENENT_TIME);
            //-----------------------------------------------------//
            if (error==0) {
                if ((rc632Api.rc632ByteIn(fifo) & 0x0f) == 0x0a) {
                    error = 0;
                } else {
                    error = 1;
                }
            }
        }

        //-----------------------------------------------------//
        rc632Api.rc632ByteOut(cmnd, (byte) 0x00);
        //-----------------------------------------------------//
        return error;
        //-----------------------------------------------------//
    }


}
