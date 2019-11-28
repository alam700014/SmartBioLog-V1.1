package com.friendlyarm.SmartReader;

/**
 * Created by fortuna on 7/6/17.
 */

//============================================Methods Defined========================================================//

//public int blockRead(int keyFlag, byte[] key, byte blockVal, byte[] readBuff)
//public int blockWrite(int keyFlag, byte[] key, byte blockVal, byte[] writeBuff)
//public int sectorRead(int keyFlag, byte[] key, byte sectorVal, byte[] readBuff)
//public int sectorWrite(int keyFlag, byte[] key, byte sectorVal, byte[] writeBuff)
//public boolean changeKeyB(byte sectorVal, byte[] keyB)
//public int sectorClean(int keyFlag, byte[] key, byte sectorVal)
//
//public int fingerTemplateCount(int keyType, byte[] keyVal, byte[] dBuf)
//public int fingerTemplateCheck(byte[] fingerTemplate)
//public int madOperation(int tVal)
//public int readFingerTemplate(int keyType, byte[] template)
//public int writeFingerTemplate(int keyType, byte[] rwKey, byte[] template, int tVal)
//public int templateKeyWrite(int tVal)


//===================================================================================================================//

public class SmartFinger {


//    unsigned char block_read(int key_flg,char *key,char blk_val,char *sv_buf)
//    {
//
//        //-------------------------------------------------------//
//        smart_card_halt();
//        //-------------------------------------------------------//
//        if(smart_card_request(info_buf))return 1;
//        if(smart_card_anticollision())return 2;
//        if(smart_card_select(info_buf))return 3;
//
//        if(smart_card_load_key(key))return 4;
//        if(smart_card_authourization_1(blk_val,key_flg))return 5;
//        if(smart_card_authourization_2())return 6;
//        if(smart_card_read(blk_val,sv_buf))return 7;
//        //-------------------------------------------------------//
//        smart_card_halt();return 0;
//        //-------------------------------------------------------//
//    }

    static byte[] info_buf = new byte[16];

    String[] key_a_sect_xx = new String[]{
            "012345", "EDCBA@", "012345", "012345",
            "012345", "012345", "012345", "012345",
            "012345", "012345", "012345", "012345",
            "012345", "012345", "012345", "012345"};

    String[] key_b_sect_xx = new String[]{
            "RESSEC", "CPS ID", "perinf", "543210",
            "FRPRN1", "FRPRN2", "FRPRN3", "FRPRN4",
            "FRPRN5", "FRPRN6", "FRPRN7", "FRPRN8",
            "FRPRN9", "FRPRN:", "FRPRN;", "FRPRN<"};


    String[] key_b_dflt = new String[]{
            "RESSEC", "CPS ID", "543210", "543210",
            "543210", "543210", "543210", "543210",
            "543210", "543210", "543210", "543210",
            "543210", "543210", "543210", "543210"};

    byte[][] accessCodes = new byte[][]
            {
                    new byte[]{(byte) 0x7F, (byte) 0x07, (byte) 0x88, (byte) 0xC1},
                    new byte[]{(byte) 0x0F, (byte) 0x07, (byte) 0x8F, (byte) 0xC1},
                    new byte[]{(byte) 0x78, (byte) 0x77, (byte) 0x88, (byte) 0xC1},
                    new byte[]{(byte) 0x08, (byte) 0x77, (byte) 0x88, (byte) 0xC1},
                    new byte[]{(byte) 0x7F, (byte) 0x00, (byte) 0xF8, (byte) 0xC1},
                    new byte[]{(byte) 0x0F, (byte) 0x00, (byte) 0xF8, (byte) 0xC1},
                    new byte[]{(byte) 0x78, (byte) 0x70, (byte) 0xF8, (byte) 0xC1},
                    new byte[]{(byte) 0x08, (byte) 0x70, (byte) 0xFF, (byte) 0xC1}
            };

    byte[] sectr1_dflt_val = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x46, (byte) 0x4F, (byte) 0x52, (byte) 0x54, (byte) 0x55, (byte) 0x4E, (byte) 0x41, (byte) 0x20,
            (byte) 0x11, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xAA, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};







    SmartCardApi smartCardApi;

    public SmartFinger(SmartCardApi smartCardApi) {
        this.smartCardApi = smartCardApi;
    }

    public SmartCardApi getSmartCardApi(){
        return smartCardApi;
    }

    public int blockRead(int keyFlag, byte[] key, byte blockVal, byte[] readBuff) {

        smartCardApi.smart_card_halt();
        if (smartCardApi.smart_card_request(info_buf) == 1) return 1;
        if (smartCardApi.smart_card_anticollision() == 1) return 2;
        if (smartCardApi.smart_card_select(info_buf) == 1) return 3;
        if (smartCardApi.smartCardLoadKey(key) == 1) return 4;
        if (smartCardApi.smartCardAuthorization_1(blockVal, keyFlag) == 1) return 5;
        if (smartCardApi.smartCardAuthorization_2() == 1) return 6;
        if (smartCardApi.smartCardRead(blockVal, readBuff) == 1) return 7;
        smartCardApi.smart_card_halt();
        return 0;
    }

//    unsigned char block_write(int key_flg,char *key,char blk_val,char *sv_buf)
//    {
//
//        //-------------------------------------------------------//
//        smart_card_halt();
//        //-------------------------------------------------------//
//        if(smart_card_request(info_buf))return 1;
//        if(smart_card_anticollision())return 2;
//        if(smart_card_select(info_buf))return 3;
//
//        if(smart_card_load_key(key))return 4;
//        if(smart_card_authourization_1(blk_val,key_flg))return 5;
//        if(smart_card_authourization_2())return 6;
//
//        if(smart_card_write(blk_val,sv_buf))return 7;
//        //-------------------------------------------------------//
//        smart_card_halt();return 0;
//        //-------------------------------------------------------//
//    }

    public int blockWrite(int keyFlag, byte[] key, byte blockVal, byte[] writeBuff) {

        smartCardApi.smart_card_halt();
        if (smartCardApi.smart_card_request(info_buf) == 1) return 1;
        if (smartCardApi.smart_card_anticollision() == 1) return 2;
        if (smartCardApi.smart_card_select(info_buf) == 1) return 3;
        if (smartCardApi.smartCardLoadKey(key) == 1) return 4;
        if (smartCardApi.smartCardAuthorization_1(blockVal, keyFlag) == 1) return 5;
        if (smartCardApi.smartCardAuthorization_2() == 1) return 6;
        if (smartCardApi.smartCardWrite(blockVal, writeBuff) == 1) return 7;
        smartCardApi.smart_card_halt();
        return 0;
    }


//    unsigned char sector_read(int key_flg,char *key,char sect_val,char *sv_buf)
//    {
//        unsigned char i = 0,j = 0,k = 0;
//        char temp_sect_rwt_buf[16]="";
//        //-------------------------------------------------------//
//        smart_card_halt();
//        //-------------------------------------------------------//
//        if(smart_card_request(info_buf))return 1;
//        if(smart_card_anticollision())return 2;
//        if(smart_card_select(info_buf))return 3;
//
//        //-------------------------------------------------------//
//        for(i = 0;i < 3;i++ )
//        {
//            //-------------------------------------------------------------------//
//            if(smart_card_load_key(key))return 4;
//            if(smart_card_authourization_1(((sect_val * 4) + i ),key_flg))return 5;
//            if(smart_card_authourization_2())return 6;
//            if(smart_card_read(((sect_val * 4) + i ),temp_sect_rwt_buf))return 7;
//
//            for (j = 0; j < 16; j++)
//            {   *(sv_buf + k++) = *(temp_sect_rwt_buf + j); 	 }
//            //-------------------------------------------------------------------//
//        }
//        //-------------------------------------------------------//
//        smart_card_halt();
//        //-------------------------------------------------------//
//        return 0;
//        //-------------------------------------------------------//
//    }

    public int sectorRead(int keyFlag, byte[] key, byte sectorVal, byte[] readBuff) {

        int i = 0, j = 0, k = 0;
        byte[] temp_sect_rwt_buf = new byte[16];
        smartCardApi.smart_card_halt();
        if (smartCardApi.smart_card_request(info_buf) == 1) return 1;
        if (smartCardApi.smart_card_anticollision() == 1) return 2;
        if (smartCardApi.smart_card_select(info_buf) == 1) return 3;

        for (i = 0; i < 3; i++) {
            //-------------------------------------------------------------------//
            if (smartCardApi.smartCardLoadKey(key) == 1) return 4;
            if (smartCardApi.smartCardAuthorization_1((byte) ((sectorVal * 4) + i), keyFlag) == 1)
                return 5;
            if (smartCardApi.smartCardAuthorization_2() == 1) return 6;
            if (smartCardApi.smartCardRead((byte) ((sectorVal * 4) + i), temp_sect_rwt_buf) == 1)
                return 7;

            for (j = 0; j < 16; j++) {
                readBuff[k++] = temp_sect_rwt_buf[j];

                //   *(sv_buf + k++) = *(temp_sect_rwt_buf + j);
            }
            //-------------------------------------------------------------------//
        }

        smartCardApi.smart_card_halt();
        return 0;
    }

//
//    unsigned char sector_write(int key_flg,char *key,char sect_val,char *sv_buf)
//    {
//
//        unsigned char i = 0,j = 0,k = 0;
//        char temp_sect_rwt_buf[16]="";
//        //-------------------------------------------------------//
//        smart_card_halt();
//        //-------------------------------------------------------//
//        if(smart_card_request(info_buf))return 1;
//        if(smart_card_anticollision())return 2;
//        if(smart_card_select(info_buf))return 3;
//
//        //-------------------------------------------------------//
//        for(i = 0;i < 3;i++ )
//        {
//            //-------------------------------------------------------------------//
//            if(smart_card_load_key(key))return 4;
//            if(smart_card_authourization_1(((sect_val * 4) + i ),key_flg))return 5;
//            if(smart_card_authourization_2())return 6;
//
//            for (j = 0; j < 16; j++)
//            {    *(temp_sect_rwt_buf + j)  = *(sv_buf + k++);	 }
//
//            if(smart_card_write(((sect_val * 4) + i ),temp_sect_rwt_buf))return 7;
//            //-------------------------------------------------------------------//
//        }
//        //-------------------------------------------------------//
//        smart_card_halt();
//        //-------------------------------------------------------//
//
//        return 0;
//        //-------------------------------------------------------//
//    }

    public int sectorWrite(int keyFlag, byte[] key, byte sectorVal, byte[] writeBuff) {

        int i = 0, j = 0, k = 0;
        byte[] temp_sect_rwt_buf = new byte[16];
        smartCardApi.smart_card_halt();
        if (smartCardApi.smart_card_request(info_buf) == 1) return 1;
        if (smartCardApi.smart_card_anticollision() == 1) return 2;
        if (smartCardApi.smart_card_select(info_buf) == 1) return 3;

        for (i = 0; i < 3; i++) {
            //-------------------------------------------------------------------//
            if (smartCardApi.smartCardLoadKey(key) == 1) return 4;
            if (smartCardApi.smartCardAuthorization_1((byte) ((sectorVal * 4) + i), keyFlag) == 1)
                return 5;
            if (smartCardApi.smartCardAuthorization_2() == 1) return 6;

            for (j = 0; j < 16; j++) {
                temp_sect_rwt_buf[j] = writeBuff[k++];
                //*(temp_sect_rwt_buf + j)  = *(sv_buf + k++);

            }

            if (smartCardApi.smartCardWrite((byte) ((sectorVal * 4) + i), temp_sect_rwt_buf) == 1)
                return 7;


            //-------------------------------------------------------------------//
        }

        smartCardApi.smart_card_halt();
        return 0;
    }
//
//    BIT chng_key_b(U8 sn, U8 const *ky)
////'sn' = Sector No.
//    {
//        char const *r4;
//        U8 i,a1;
//        char kb[6] = {"      "};
//
//        tsk_lock();
//        for(i = 0; i != 3; i++)
//        {
//            r4 = keyb_chk_tbl[i];
//            for(a1 = 0; a1 != 6; a1++){*(kb + a1) = *r4++;}
//            if(!block_read(KEY_B, kb, ((sn * 4) + 0), rd_tmp_buf))break;	//Try to Read 1st Block of Sector 'sn'
//        }
//        if(i == 3)
//        {
//            tsk_unlock();
//            srl0_put_char('a');	//+++++for testing purpose+++++
//            return __FALSE;
//        }
//        if(!block_read(KEY_A,"012345", ((sn * 4) + 3), rd_tmp_buf))
//        {
//            //-----------------------------------------------------------------------//
//            for (i = 0; i < 6;  i++){*(key_tmp_buf + i) = *(key_a + i);}
//            for (i = 6; i < 10; i++){*(key_tmp_buf + i) = *(rd_tmp_buf + i);}
//            for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *ky++;}
//            //-----------------------------------------------------------------------//
//            if(block_write(KEY_B, kb, ((sn * 4) + 3), key_tmp_buf))
//            {
//                tsk_unlock();
//                srl0_put_char('b');	//+++++for testing purpose+++++
//                return __FALSE;
//            }
//            else
//            {
//                tsk_unlock();
//                srl0_put_char('c');	//+++++for testing purpose+++++
//                return __TRUE;
//            }
//            //-----------------------------------------------------------------------//
//        }
//        tsk_unlock();
//        srl0_put_char('d');	//+++++for testing purpose+++++
//        return __FALSE;
//    }
//    char const *keyb_chk_tbl[] = {
//        "543210",
//                "PERINF",
//                "perinf"
//    };

    String[] keyb_chk_tbl = new String[]{"543210", "PERINF", "perinf"};
    // String[] keyb_chk_tbl = new String[]{"543210", "PERINF", "123456"};
    static byte[] rd_tmp_buf = new byte[16];
    static byte[] key_tmp_buf = new byte[16];
    String key_a = "012345";
    final static int KEY_A = 1;
    final static int KEY_B = 0;

    public boolean changeKeyB(byte sectorVal, byte[] keyB) {

        byte[] kb = new byte[6];
        byte[] r4 = new byte[6];
        int i, a1;

        for (i = 0; i != 3; i++) {
            r4 = keyb_chk_tbl[i].getBytes();
            for (a1 = 0; a1 != 6; a1++) {
                kb[a1] = r4[a1];

                //*(kb + a1) = *r4++;

            }
            // if(!block_read(KEY_B, kb, ((sn * 4) + 0), rd_tmp_buf))break;	//Try to Read 1st Block of Sector 'sn'

            if (blockRead(KEY_B, kb, (byte) ((sectorVal * 4) + 0), rd_tmp_buf) == 0)
                break;

        }

        if (i == 3) return false;

        String strKeyA = "012345";
        byte[] key_a_bytes = new byte[6];
        key_a_bytes = key_a.getBytes();

        if (blockRead(KEY_A, strKeyA.getBytes(), (byte) ((sectorVal * 4) + 3), rd_tmp_buf) == 0) {
            //-----------------------------------------------------------------------//
            for (i = 0; i < 6; i++) {
                key_tmp_buf[i] = key_a_bytes[i];
                //  *(key_tmp_buf + i) = *(key_a + i);
            }
            for (i = 6; i < 10; i++) {
                key_tmp_buf[i] = rd_tmp_buf[i];
                // *(key_tmp_buf + i) = *(rd_tmp_buf + i);
            }

            int j = 0;
            for (i = 10; i < 16; i++, j++) {
                key_tmp_buf[i] = keyB[j];
                // *(key_tmp_buf + i) = *ky++;
            }

            //key a:303132333435
            // access code:7F0788C1
            //key b:313233343536
            //perinf

            //  Log.d("TEST", "Sector 2 Trailing Block:" +bytesToHex(key_tmp_buf));


            //-----------------------------------------------------------------------//
//
            if (blockWrite(KEY_B, kb, (byte) ((sectorVal * 4) + 3), key_tmp_buf) == 1) {
                // tsk_unlock();
                // srl0_put_char('b');	//+++++for testing purpose+++++
                return false;
            } else {
                // tsk_unlock();
                // srl0_put_char('c');	//+++++for testing purpose+++++
                return true;
            }

            //-----------------------------------------------------------------------//
        }

        return false;
    }
//
//    unsigned char sector_clean(int key_flg,char *key,char sect_val)
//    {
//
//        unsigned char i = 0,j = 0;
//        char temp_sect_rwt_buf[16]="";
//        //-------------------------------------------------------//
//        smart_card_halt();
//        //-------------------------------------------------------//
//        if(smart_card_request(info_buf))return 1;
//        if(smart_card_anticollision())return 2;
//        if(smart_card_select(info_buf))return 3;
//
//        //-------------------------------------------------------//
//        for(i = 0;i < 3;i++ )
//        {
//            //-------------------------------------------------------------------//
//            if(smart_card_load_key(key))return 4;
//            if(smart_card_authourization_1(((sect_val * 4) + i ),key_flg))return 5;
//            if(smart_card_authourization_2())return 6;
//
//            for (j = 0; j < 16; j++){ *(temp_sect_rwt_buf + j)  = 0x00;	 }
//
//            if(smart_card_write(((sect_val * 4) + i ),temp_sect_rwt_buf))return 7;
//            //-------------------------------------------------------------------//
//        }
//        //-------------------------------------------------------//
//        smart_card_halt();
//        //-------------------------------------------------------//
//
//        return 0;
//        //-------------------------------------------------------//
//    }


    public int sectorClean(int keyFlag, byte[] key, byte sectorVal) {

        int i = 0, j = 0;
        byte[] temp_sect_rwt_buf = new byte[16];
        smartCardApi.smart_card_halt();
        if (smartCardApi.smart_card_request(info_buf) == 1) return 1;
        if (smartCardApi.smart_card_anticollision() == 1) return 2;
        if (smartCardApi.smart_card_select(info_buf) == 1) return 3;

        for (i = 0; i < 3; i++) {
            //-------------------------------------------------------------------//
            if (smartCardApi.smartCardLoadKey(key) == 1) return 4;
            if (smartCardApi.smartCardAuthorization_1(((byte) ((sectorVal * 4) + i)), keyFlag) == 1)
                return 5;
            if (smartCardApi.smartCardAuthorization_2() == 1) return 6;

            for (j = 0; j < 16; j++) {
                temp_sect_rwt_buf[j] = 0x00;
                //  *(temp_sect_rwt_buf + j)  = 0x00;
            }

            if (smartCardApi.smartCardWrite((byte) ((sectorVal * 4) + i), temp_sect_rwt_buf) == 1)
                return 7;
            //-------------------------------------------------------------------//
        }

        smartCardApi.smart_card_halt();
        return 0;
    }


    //===================================Finger Template Read Write Functions====================================//

//    nsigned char fing_template_check(char *fig_tmp)
//    {
//        unsigned char  i = 0,app_i = 0,app_j = 0;
//        unsigned char  fig_no_1 = 0,fig_no_2 = 0;
//        //---------------------------------------------------//
//        app_i = 0; app_j = 0;
//        for (i = 24; i < 36; i++)
//        {
//            if(*(fig_tmp + i)	== APP_TYPE ){app_i++;}
//            if(*(fig_tmp + i)	== APP_CODE ){app_j++;}
//        }
//        if(app_i == 6 && app_j == 6) {   fig_no_1++; }
//        //---------------------------------------------------//
//        app_i = 0; app_j = 0;
//        for (i = 36; i < 48; i++)
//        {
//            if(*(fig_tmp + i)	== APP_TYPE ){app_i++;}
//            if(*(fig_tmp + i)	== APP_CODE ){app_j++;}
//        }
//
//        if(app_i == 6 && app_j == 6) {   fig_no_2++; }
//        //---------------------------------------------------//
//        fig_no_1 =  ((fig_no_1 << 4) & 0xf0) | fig_no_2;
//        //---------------------------------------------------//
//        //lfcr();lfcr();
//        //srl0_put_byte(fig_no);srl0_put_char(' ');srl0_put_char(' ');srl0_put_char(' ');
//        //for (i = 0; i < 48; i++){   srl0_put_byte(*(fig_tmp + i));}
//        //lfcr();lfcr();
//        //---------------------------------------------------//
//        return fig_no_1;
//
//    }

    public final static byte APP_TYPE = 0x48;
    public final static byte APP_CODE = 0x02;

//    unsigned char fing_template_count(char key_type ,char *key_val ,char *d_buf)
//    {
//        if(sector_read(key_type,key_val,0,d_buf)){ return 0xff; }
//        return (fing_template_check(d_buf));
//
//    }

    //16 bytes Sector 0 block 0
    //4802 4802 4802 4802 480248024802480248024802480248024802480248024802

    public int fingerTemplateCount(int keyType, byte[] keyVal, byte[] dBuf) {
        if (sectorRead(keyType, keyVal, (byte) 0x00, dBuf) == 1) return 0xff;
        return fingerTemplateCheck(dBuf);
    }

    public int fingerTemplateCheck(byte[] fingerTemplate) {

        int i = 0, app_i = 0, app_j = 0;
        int fig_no_1 = 0, fig_no_2 = 0;

        for (i = 24; i < 36; i++) {
            if (fingerTemplate[i] == APP_TYPE) {
                app_i++;
            }
            if (fingerTemplate[i] == APP_CODE) {
                app_j++;
            }
        }

        if (app_i == 6 && app_j == 6) {
            fig_no_1++;
        }
//        //---------------------------------------------------//
        app_i = 0;
        app_j = 0;
        for (i = 36; i < 48; i++) {
            if (fingerTemplate[i] == APP_TYPE) {
                app_i++;
            }
            if (fingerTemplate[i] == APP_CODE) {
                app_j++;
            }
        }
        if (app_i == 6 && app_j == 6) {
            fig_no_2++;
        }
        fig_no_1 = ((fig_no_1 << 4) & 0xf0) | fig_no_2;

        //======================================Modified By Ankit Kumar==========================================//

//        if (fig_no_1 == 0) {
//           //No finger Found In Card
//        } else if (fig_no_1 == 16) {
//            //First Finger Found in Card
//        } else if (fig_no_1 == 17) {
//            //Two Fingers Found In Card
//        }else if(fig_no_1==1){
//            //Second Finger Found In Card
//        }

        //========================================================================================================//

        return fig_no_1;
    }

//    char *sector_key(char key_type,char sect_val,char *temp)
//    {
//        unsigned char ii = 0;
//
//        for(ii = 0;ii < 6;ii++)
//        {
//            //if(!key_type){*(temp + ii) = *(key_b_sect_xx[sect_val] + ii);}
//            if(!key_type){*(temp + ii) = *(lpc_key_b_tbl + sect_val*6 + ii);}
//            else {*(temp + ii) = *(key_a_sect_xx[sect_val] + ii);}
//
//        }
//
//
//        return temp;
//    }


    public byte[] sectorKey(int keyType, byte sectorVal, byte[] temp) {

        int ii;
        byte[] keya = new byte[6];
        byte[] keyb = new byte[6];
        keya = key_a_sect_xx[sectorVal].getBytes();
        keyb = key_b_sect_xx[sectorVal].getBytes();

        for (ii = 0; ii < 6; ii++) {
            if (keyType == 0) {
                temp[ii] = keyb[ii];
                //if(!key_type){*(temp + ii) = *(key_b_sect_xx[sect_val] + ii);}
                //*(temp + ii) =*(lpc_key_b_tbl + sect_val * 6 + ii);
            } else {
                temp[ii] = keya[ii];
                //*(temp + ii) =*(key_a_sect_xx[sect_val] + ii);
            }
        }
        return temp;
    }

//    unsigned char mad_operation(unsigned char t_val)
//    {
//        unsigned char i = 0;
//        //-------------------------------------------------------//
//        if(!sector_read(KEY_B,"RESSEC",0,rd_sect_buf))
//        {
//            //-------------------------------------------------------//
//            switch(t_val)
//            {
//                case 0: 	for (i = 24; i < 36;  i++)
//                {*(rd_sect_buf + i) = *(fig_temp_info + (i - 24));}  break;
//
//                case 1: 	for (i = 24; i < 36;  i++)
//                {*(rd_sect_buf + i) = 0x00;}  break;
//
//                case 2: 	for (i = 36; i < 48;  i++)
//                {*(rd_sect_buf + i) = *(fig_temp_info + (i - 36));}  break;
//
//                case 3: 	for (i = 36; i < 48;  i++)
//                {*(rd_sect_buf + i) = 0x00;}  break;
//
//                default:	 return 1;
//            }
//            //-------------------------------------------------------//
//
//            //lfcr();srl0_put_byte(0);srl0_put_char('-');
//            //for (i = 0; i < 48; i++){ srl0_put_byte(*(rd_sect_buf + i));  }
//            //lfcr();
//
//
//            for (i = 0; i < 16;  i++){ *(rd_tmp_buf + i ) = *(rd_sect_buf + (i + 16)); }
//            if(block_write(KEY_B,"RESSEC",1, rd_tmp_buf)){ return 1;}
//
//            for (i = 0; i < 16;  i++){ *(rd_tmp_buf + i ) = *(rd_sect_buf + (i + 32)); }
//            if(block_write(KEY_B,"RESSEC",2, rd_tmp_buf)){ return 1;}
//
//            //-------------------------------------------------------//
//        }
//        else{ return 1;}
//        //-------------------------------------------------------//
//        return 0;
//        //-------------------------------------------------------//
//    }


    static byte[] rd_sect_buf = new byte[48];

    byte[] fig_temp_info = new byte[]{0x48, 0x02, 0x48, 0x02, 0x48, 0x02,
            0x48, 0x02, 0x48, 0x02, 0x48, 0x02};

    public int madOperation(int tVal) {

        int i = 0;
        String strSector0Key = "RESSEC";
        byte[] key = new byte[6];
        key = strSector0Key.getBytes();

        if (sectorRead(KEY_B, key, (byte) 0x00, rd_sect_buf) == 0) {
            //-------------------------------------------------------//
            switch (tVal) {
                case 0:
                    for (i = 24; i < 36; i++) {
                        rd_sect_buf[i] = fig_temp_info[i - 24];
                        // *(rd_sect_buf + i) =*(fig_temp_info + (i - 24));
                    }
                    break;
                case 1:
                    for (i = 24; i < 36; i++) {
                        rd_sect_buf[i] = 0x00;
                        //*(rd_sect_buf + i) = 0x00;
                    }
                    break;
                case 2:
                    for (i = 36; i < 48; i++) {
                        rd_sect_buf[i] = fig_temp_info[i - 36];
                        //*(rd_sect_buf + i) =*(fig_temp_info + (i - 36));
                    }
                    break;
                case 3:
                    for (i = 36; i < 48; i++) {
                        rd_sect_buf[i] = 0x00;
                        //*(rd_sect_buf + i) = 0x00;
                    }
                    break;
                default:
                    return 1;
            }
            //-------------------------------------------------------//

            //lfcr();srl0_put_byte(0);srl0_put_char('-');
            //for (i = 0; i < 48; i++){ srl0_put_byte(*(rd_sect_buf + i));  }
            //lfcr();

            for (i = 0; i < 16; i++) {
                rd_tmp_buf[i] = rd_sect_buf[i + 16];
                // *(rd_tmp_buf + i) =*(rd_sect_buf + (i + 16));
            }
            if (blockWrite(KEY_B, key, (byte) 0x01, rd_tmp_buf) == 1) {
                return 1;
            }

            for (i = 0; i < 16; i++) {
                rd_tmp_buf[i] = rd_sect_buf[i + 32];
                //*(rd_tmp_buf + i) =*(rd_sect_buf + (i + 32));
            }
            if (blockWrite(KEY_B, key, (byte) 0x02, rd_tmp_buf) == 1) {
                return 1;
            }

            //-------------------------------------------------------//
        } else {
            return 1;
        }
        //-------------------------------------------------------//
        return 0;
    }


    //===========================================================================================================//


    //========================================Read Finger Template==================================================//

//
//    unsigned char read_fing_template (char key_typ,char *template)
//    {
//        unsigned int	i = 0,j = 0;
//        //--------------------------------------------------------------------------------//
//        for (i = 0; i < 580; i++){  *(template + i) = 0x00;	}
//        //--------------------------------------------------------------------------------//
//        switch( fing_template_count(KEY_B,"RESSEC",rd_sect_buf))
//        {
//            case 0x00:
//                return 0x10;
//
//            case 0x10:
//                for (j = 0; j < 6; j++)
//                {
//                    if(sector_read(key_typ,sector_key(key_typ,(4 + j),key_tmp),(4 + j),rd_sect_buf))
//                    {return 1;}else{ for (i = 0; i < 48; i++){*(template+i+(48 * j))=*(rd_sect_buf + i);} }
//                }
//                return 0x11;
//
//            case 0x01:
//                for (j = 6; j < 12; j++)
//                {
//                    if(sector_read(key_typ,sector_key(key_typ,(4 + j),key_tmp),(4 + j),rd_sect_buf))
//                    {return 1;}else{for (i = 0; i < 48; i++){*(template+i+(48 * (j - 6)))=*(rd_sect_buf + i);}			 }
//                }
//                return 0x12;
//
//            case 0x11:
//                for (j = 0; j < 12; j++)
//                {
//                    if(sector_read(key_typ,sector_key(key_typ,(4 + j),key_tmp),(4 + j),rd_sect_buf))
//                    {return 1;}else{ for (i = 0; i < 48; i++){*(template+i+(48 * j))=*(rd_sect_buf + i);} }
//                }
//                return 0x13;
//
//            default: return 1;
//
//        }
//        //--------------------------------------------------------------------------------//
//        //--------------------------------------------------------------------------------//
//    }

    static byte key_tmp[] = new byte[6];

    public int readFingerTemplate(int keyType, byte[] template) {

        int i = 0, j = 0;
        String strSector0Key = "RESSEC";
        byte[] key = new byte[6];
        key = strSector0Key.getBytes();

        for (i = 0; i < 580; i++) {
            template[i] = 0x00;
        }

        switch (fingerTemplateCount(KEY_B, key, rd_sect_buf)) {
            case 0x00:
                return 0x10;

            case 0x10:
                for (j = 0; j < 6; j++) {
                    if (sectorRead(keyType, sectorKey(keyType, (byte) (4 + j), key_tmp), (byte) (4 + j), rd_sect_buf) == 1) {
                        return 1;
                    } else {
                        for (i = 0; i < 48; i++) {
                            template[i + (48 * j)] = rd_sect_buf[i];
                            //*(template+i+(48 * j))=*(rd_sect_buf + i);
                        }
                    }
                }
                return 0x11;

            case 0x01:
                for (j = 6; j < 12; j++) {
                    if (sectorRead(keyType, sectorKey(keyType, (byte) (4 + j), key_tmp), (byte) (4 + j), rd_sect_buf) == 1) {
                        return 1;
                    } else {
                        for (i = 0; i < 48; i++) {
                            template[i + (48 * (j - 6))] = rd_sect_buf[i];
                            // *(template+i+(48 * (j - 6)))=*(rd_sect_buf + i);
                        }
                    }
                }
                return 0x12;

            case 0x11:
                for (j = 0; j < 12; j++) {
                    if (sectorRead(keyType, sectorKey(keyType, (byte) (4 + j), key_tmp), (byte) (4 + j), rd_sect_buf) == 1) {
                        return 1;
                    } else {
                        for (i = 0; i < 48; i++) {
                            template[i + (48 * j)] = rd_sect_buf[i];
                            // *(template+i+(48 * j))=*(rd_sect_buf + i);
                        }
                    }
                }
                return 0x13;

            default:
                return 1;
        }
    }
    //==============================================================================================================//

    //=======================================Finger Template Write==================================================//

//    unsigned char write_fing_template (char key_typ,char *rw_key,char *templ,char t_val)
//    {
//        unsigned int	i = 0,j = 0;
//        //--------------------------------------------------------------------------------//
//        //--------------------------------------------------------------------------------//
//        switch(t_val)
//        {
//            case 1:
//                //------------------------------------------------------------------------------//
//                if(mad_operation(0)){ return 1; }
//                for (j = 0; j < 6; j++)
//                {
//                    for (i = 0; i < 48; i++){*(rd_sect_buf + i) = *(templ+i+(48 * j));}
//                    if(sector_write(key_typ,rw_key,(4 + j),rd_sect_buf)){return 2;}
//
//                }
//                if(template_key_write(1)){ return 3; }
//                return 0;
//            //------------------------------------------------------------------------------//
//            case 2:
//                //------------------------------------------------------------------------------//
//                if(mad_operation(2)){ return 1; }
//                for (j = 6; j < 12; j++)
//                {
//                    for (i = 0; i < 48; i++){*(rd_sect_buf + i) = *(templ+i+(48 * (j - 6)));}
//                    if(sector_write(key_typ,rw_key,(4 + j),rd_sect_buf)){return 2;}
//                }
//                if(template_key_write(2)){ return 3; }
//                return 0;
//            //------------------------------------------------------------------------------//
//            default: return 4;
//
//        }
//        //--------------------------------------------------------------------------------//
//        //--------------------------------------------------------------------------------//
//
//    }

    public int writeFingerTemplate(int keyType, byte[] rwKey, byte[] template, int tVal) {

        int i = 0, j = 0;

        switch (tVal) {
            case 1:
                //------------------------------------------------------------------------------//
                //if (madOperation(0) == 1) {
                if ((madOperation(0) != 0)) {
                    return 1;
                }
                for (j = 0; j < 6; j++) {
                    for (i = 0; i < 48; i++) {
                        rd_sect_buf[i] = template[i + (48 * j)];
                        // *(rd_sect_buf + i) = *(templ+i+(48 * j));
                    }
                    if (sectorWrite(keyType, rwKey, (byte) (4 + j), rd_sect_buf) != 0) {
                        return 2;
                    }
                }
                // if (templateKeyWrite(1) == 1) {
                if (templateKeyWrite(1) != 0) {
                    return 3;
                }
                return 0;
            //------------------------------------------------------------------------------//
            case 2:
                //------------------------------------------------------------------------------//
                if (madOperation(2) != 0) {
                    return 1;
                }
                for (j = 6; j < 12; j++) {
                    for (i = 0; i < 48; i++) {
                        rd_sect_buf[i] = template[i + (48 * (j - 6))];
                        // *(rd_sect_buf + i) = *(templ+i+(48 * (j - 6)));
                    }
                    if (sectorWrite(keyType, rwKey, (byte) (4 + j), rd_sect_buf) != 0) {
                        return 2;
                    }
                }
                if (templateKeyWrite(2) != 0) {
                    return 3;
                }
                return 0;
            //------------------------------------------------------------------------------//
            default:
                return 4;
        }
    }

    //==============================================================================================================//

    //==========================================Template Key Write==================================================//

//    unsigned char template_key_write(unsigned char t_val)
//    {
//
//        unsigned char  i = 0,j = 0;
//        switch(t_val)
//        {
//            //-----------------------------------------------------------------------//
//            case 1:
//
//                for (j = 0; j < 6; j++)
//                {
//                    if(!block_read(KEY_A,"012345",((((j + 4) * 4) + 3)),rd_tmp_buf))
//                    {
//                        //-----------------------------------------------------------------------//
//                        for (i = 0; i < 6;  i++){*(key_tmp_buf + i) = *(key_a + i);}
//                        for (i = 6; i < 10; i++){*(key_tmp_buf + i) = *(rd_tmp_buf + i);}
//                        //for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *(key_b_sect_xx[4 + j] + (i - 10));}
//                        for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *(lpc_key_b_tbl + (4 + j)*6 + (i - 10));}
//                        //-----------------------------------------------------------------------//
//                        if(block_write(KEY_B,"543210",((((j + 4) * 4) + 3)), key_tmp_buf)){ return 1;}
//                        //-----------------------------------------------------------------------//
//                    }
//                    else { return 1;}
//                }
//
//                return 0;
//            //-----------------------------------------------------------------------//
//            //-----------------------------------------------------------------------//
//            case 2:
//
//                for (j = 6; j < 12; j++)
//                {
//                    if(!block_read(KEY_A,"012345",((((j + 4) * 4) + 3)),rd_tmp_buf))
//                    {
//                        //-----------------------------------------------------------------------//
//                        for (i = 0; i < 6;  i++){*(key_tmp_buf + i) = *(key_a + i);}
//                        for (i = 6; i < 10; i++){*(key_tmp_buf + i) = *(rd_tmp_buf + i);}
//                        //for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *(key_b_sect_xx[4 + j] + (i - 10));}
//                        for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *(lpc_key_b_tbl + (4 + j)*6 + (i - 10));}
//                        //-----------------------------------------------------------------------//
//                        if(block_write(KEY_B,"543210",((((j + 4) * 4) + 3)), key_tmp_buf)){ return 1;}
//                        //-----------------------------------------------------------------------//
//                    }
//                    else { return 1;}
//                }
//
//                return 0;
//            //-----------------------------------------------------------------------//
//            //-----------------------------------------------------------------------//
//            default: return 1;
//            //-----------------------------------------------------------------------//
//        }
//
//    }

    //==================Functions Created By Ankit Kumar For Template Key Write=============//

    public int writeTemplateKey(byte blockVal,byte[] keyb){

        //int error=-1;
        int i;

        String strKeyA = "012345";
        String strKeyB = "543210";

        byte[] keyA = new byte[6];
        byte[] keyB = new byte[6];
       // byte[] keyb = new byte[6];

        keyA = strKeyA.getBytes();
        keyB = strKeyB.getBytes();

       // if (blockRead(KEY_A, keyA, (byte) ((((j + 4) * 4) + 3)), rd_tmp_buf) == 0) {

        if (blockRead(KEY_A, keyA, blockVal, rd_tmp_buf) == 0) {
            //-----------------------------------------------------------------------//
            for (i = 0; i < 6; i++) {
                key_tmp_buf[i] = keyA[i];
                //  *(key_tmp_buf + i) = *(key_a + i);
            }
            for (i = 6; i < 10; i++) {
                key_tmp_buf[i] = rd_tmp_buf[i];
                // *(key_tmp_buf + i) = *(rd_tmp_buf + i);

            }
            //for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *(key_b_sect_xx[4 + j] + (i - 10));}
            for (i = 10; i < 16; i++) {

                //*(key_tmp_buf + i) = *(key_b_sect_xx[4 + j] + (i - 10));

                //=======================Commented On 28-06-2018=======================//
                //keyb = key_b_sect_xx[4 + j].getBytes();
                //=====================================================================//

                key_tmp_buf[i] = keyb[i - 10];

                //  *(key_tmp_buf + i) = *(lpc_key_b_tbl + (4 + j)*6 + (i - 10));


            }
            //-----------------------------------------------------------------------//
            if (blockWrite(KEY_B, keyB, blockVal, key_tmp_buf) == 1) {
                return 1;
            }
            //-----------------------------------------------------------------------//
        } else {
            return 1;
        }
        return 0;
    }

    //=====================================================================================//

    //===================Function Created By Ankit Kumar For Sector 0,2,4,5,6,7,8,9,10,11,12,13,14,15  Refresh ==================//

    public int sectorRefresh(byte sectorVal,byte[] keya,byte[] keyb,byte[] keybdflt) {

//        byte[] keya = new byte[6];
//        byte[] keybdflt = new byte[6];
//        byte[] keyb = new byte[6];
//
//        keya = key_a_sect_xx[sectorVal].getBytes();
//        keybdflt = key_b_dflt[sectorVal].getBytes();
//        keyb = key_b_sect_xx[sectorVal].getBytes();


        switch (sectorVal) {

            case 0:

                //for Sector-0......First 16-bytes should remain UnChanged

                byte[] mad0 = new byte[]{0x55, 0x01, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                byte[] mad1 = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

                if (blockWrite(KEY_B, keyb, (byte) 0x01, mad0) != 0) {
                    return 1;
                }

                if (blockWrite(KEY_B, keyb, (byte) 0x02, mad1) != 0) {
                    return 1;
                }
                break;

            case 1:
                break;

            case 3:
                break;

            default:

                boolean x = false;
                int i = blockRead(KEY_B, keybdflt, (byte) ((sectorVal * 4) + 3), rd_tmp_buf);
                if (i!=0) {
                    i = blockRead(KEY_B, keyb, (byte) ((sectorVal * 4) + 3), rd_tmp_buf);
                    if (i != 0) {

                        if (sectorVal == 0x02) {
                            String strSector2KeyB = "PERINF";
                            byte[] key = new byte[6];
                            key = strSector2KeyB.getBytes();
                            i = blockRead(KEY_B, key, (byte) ((sectorVal * 4) + 3), rd_tmp_buf);
                            x = true;
                        }
                    }

                    if (i == 0) {

                        //The Block is Successfully Read with Template KEY-B....Change KEY-B First

                        //Access code for sector 1
                        //787788C1
                        byte[] accessCode1 = accessCodes[2];

                        //Access code for all sectors 0,2,3,4,5,6,7,8,9,10,11,12,13,14,15
                        //7F0788C1
                        byte[] accessCode2 = accessCodes[0];

                        for (i = 0; i < 6; i++) {
                            // *(key_tmp_buf + i) =*(key_a + i);
                            key_tmp_buf[i] = keya[i];
                        }

                        if (sectorVal == 0x01) {
                            int z = 0;
                            for (i = 6; i < 10; i++) {
                                // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                                key_tmp_buf[i] = accessCode1[z++];
                            }
                        } else {
                            int z = 0;
                            for (i = 6; i < 10; i++) {
                                // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                                key_tmp_buf[i] = accessCode2[z++];
                            }
                        }

                        for (i = 10; i < 16; i++) {
                            //*(key_tmp_buf + i) =*(key_b + (i - 10));
                            key_tmp_buf[i] = keybdflt[i - 10];
                        }


                        if (x) {
                            // i = sc_blk_wr(KEY_B, "PERINF", ((sv * 4) + 3), gp_data_arr2);
                            String strSector2KeyB = "PERINF";
                            byte[] key = new byte[6];
                            key = strSector2KeyB.getBytes();
                            i = blockWrite(KEY_B, key, (byte) ((sectorVal * 4) + 3), key_tmp_buf);
                        } else {
                            //i = sc_blk_wr(KEY_B, (lpc_key_b_tbl + sv * 6), ((sv * 4) + 3), gp_data_arr2);
                            i = blockWrite(KEY_B, keyb, (byte) ((sectorVal * 4) + 3), key_tmp_buf);
                        }

                        if (i == 0) {

                            //For other Sectors
                            byte[] zeroBytes = new byte[48];

                            for (int j = 0; j < 48; j++) {
                                zeroBytes[j] = 0x00;
                            }

                            if (sectorWrite(KEY_B, keybdflt, sectorVal, zeroBytes) != 0) {
                                return 1;
                            }
                        }
                    }
                }

                break;
        }
        return 0;
    }

    //=================================================================================================//

    //===================Function Created By Ankit Kumar For Sector 1 Refresh ========================//

    public int sector_1_Refresh(byte sectorVal,byte[] keya,byte[] keyb,byte[] cardId){

        byte[] block4 = new byte[16];
        int l;
        for (l = 0; l < 8; l++) {
            block4[l] = cardId[l];
        }
        for (l = 8; l < 16; l++) {
            block4[l] = sectr1_dflt_val[l];
        }
        if (blockWrite(KEY_B, keyb, (byte) 0x04, block4) != 0) {
            return 1;
        }

        byte[] block5 = new byte[]{(byte) 0x11, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xAA, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        byte[] block6 = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

        if (blockWrite(KEY_B, keyb, (byte) 0x05, block5) != 0) {
            return 1;
        }

        if (blockWrite(KEY_B, keyb, (byte) 0x06, block6) != 0) {
            return 1;
        }
        return 0;
    }

    //=================================================================================================//

    //======================Function Created By Ankit Kumar For Card Init For Sector No 0,2,3,4,5,6,7,8,9,10,11,12,13,14,15================================//

    public int sectorInit(byte sectorVal,byte[] factKey,byte[] keyaf,byte[] accessCode,byte[] keybf) {

        //If tf=true set new Key a,Access Code,Key B
        //If false don't set new Key a,Access Code,Key B

        //Step 4:Start initializing all sectors trailing block from 0 to 15 one by one,change Key-A,Access Code,Key-B
        //Step 5:Read trailing block of sector n where n=0 to 15 with Key-A(Factory Key A[FFFFFFFFFFFF])
        //Step 6:Write Fortuna Key A ,Access Code,Key-B in trailing block with Key-A(Factory Key A[FFFFFFFFFFFF])
        //Access code for all sectors except 1 is 7F0788C1 and  for Sector 1 is 787788C1
        //Step 7:Sector 0 Write/MAD Write.Write Block 1 and Block 2 of Sector 0 with 5501000500000000000000000000000000000000000000000000000000000000 with Key-B(Fortuna Key-B[RESSEC])
        //Step 8:Sector 1 Write/Card Id Write.Write 8 bytes Card Id in Sector 0 Block 0 and rest 8 bytes with default value ie 464F5254554E4120 with Key-B(Fortuna Key-B[CPS ID])
        //Step 9:Sector 2 to 15 Write.Write 48 bytes with zeroes to each sector with Fortuna Key-B of respective sectors.


            int i = 0;

            if (blockRead(KEY_A, factKey, (byte)((sectorVal * 4) + 3), key_tmp_buf) == 0) {

                for (i = 0; i < 6; i++) {
                    // *(key_tmp_buf + i) =*(key_a + i);
                    key_tmp_buf[i] = keyaf[i];
                }

                if (sectorVal == 0x01) {
                    int z = 0;
                    for (i = 6; i < 10; i++) {
                        // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                        key_tmp_buf[i] = accessCode[z++];
                    }
                } else {
                    int z = 0;
                    for (i = 6; i < 10; i++) {
                        // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                        key_tmp_buf[i] = accessCode[z++];
                    }
                }

                for (i = 10; i < 16; i++) {
                    //*(key_tmp_buf + i) =*(key_b + (i - 10));
                    key_tmp_buf[i] = keybf[i - 10];
                }

                if (blockWrite(KEY_A, factKey, (byte)((sectorVal * 4) + 3), key_tmp_buf) != 0) {
                    return 1;
                }

                switch (sectorVal) {

                    case 0:

                        //for Sector-0......First 16-bytes should remain UnChanged

                        byte[] mad0 = new byte[]{0x55, 0x01, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        byte[] mad1 = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

                        if (blockWrite(KEY_B, keybf, (byte) 0x01, mad0) != 0) {
                            return 1;
                        }

                        if (blockWrite(KEY_B, keybf, (byte) 0x02, mad1) != 0) {
                            return 1;
                        }

                        break;

                    default:

                        //For other Sectors
                        byte[] zeroBytes = new byte[48];

                        for (int j = 0; j < 48; j++) {
                            zeroBytes[j] = 0x00;
                        }

                        if (sectorWrite(KEY_B, keybf, sectorVal, zeroBytes) != 0) {
                            return 1;
                        }

                        break;
                }

            } else {
                return 1;
            }

        return 0;
    }

    //==================================================================================================================//


    //======================Function Created By Ankit Kumar For Card Init For Sector No 1================================//

    public int sectorInit_1(byte sectorVal,byte[] factKey,byte[] keyaf,byte[] accessCode,byte[] keybf,byte[] cardId){

        int i = 0;

        if (blockRead(KEY_A, factKey, (byte) ((sectorVal * 4) + 3), key_tmp_buf) == 0) {

            for (i = 0; i < 6; i++) {
                // *(key_tmp_buf + i) =*(key_a + i);
                key_tmp_buf[i] = keyaf[i];
            }

            if (sectorVal == 0x01) {
                int z = 0;
                for (i = 6; i < 10; i++) {
                    // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                    key_tmp_buf[i] = accessCode[z++];
                }
            } else {
                int z = 0;
                for (i = 6; i < 10; i++) {
                    // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                    key_tmp_buf[i] = accessCode[z++];
                }
            }

            for (i = 10; i < 16; i++) {
                //*(key_tmp_buf + i) =*(key_b + (i - 10));
                key_tmp_buf[i] = keybf[i - 10];
            }

            if (blockWrite(KEY_A, factKey, (byte)((sectorVal * 4) + 3), key_tmp_buf) != 0) {
                return 1;
            }

            switch (sectorVal) {

                case 1:

                    byte[] block4 = new byte[16];
                    int l;
                    for (l = 0; l < 8; l++) {
                        block4[l] = cardId[l];
                    }
                    for (l = 8; l < 16; l++) {
                        block4[l] = sectr1_dflt_val[l];
                    }
                    if (blockWrite(KEY_B, keybf, (byte) 0x04, block4) != 0) {
                        return 1;
                    }

                    byte[] block5 = new byte[]{(byte) 0x11, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xAA, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
                    byte[] block6 = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};


                    if (blockWrite(KEY_B, keybf, (byte) 0x05, block5) != 0) {
                        return 1;
                    }

                    if (blockWrite(KEY_B, keybf, (byte) 0x06, block6) != 0) {
                        return 1;
                    }

                    break;

                default:

                    break;
            }

        } else {
            return 1;
        }

        return 0;
    }

    //==================================================================================================================//

    public int templateKeyWrite(int tVal) {

        int i = 0, j = 0;

        String strKeyA = "012345";
        String strKeyB = "543210";

        byte[] keyA = new byte[6];
        byte[] keyB = new byte[6];

        keyA = strKeyA.getBytes();
        keyB = strKeyB.getBytes();

        switch (tVal) {
            //-----------------------------------------------------------------------//
            case 1:

                byte[] keyb = new byte[6];
                for (j = 0; j < 6; j++) {
                    if (blockRead(KEY_A, keyA, (byte) ((((j + 4) * 4) + 3)), rd_tmp_buf) == 0) {
                        //-----------------------------------------------------------------------//
                        for (i = 0; i < 6; i++) {
                            key_tmp_buf[i] = keyA[i];
                            //  *(key_tmp_buf + i) = *(key_a + i);
                        }
                        for (i = 6; i < 10; i++) {
                            key_tmp_buf[i] = rd_tmp_buf[i];
                            // *(key_tmp_buf + i) = *(rd_tmp_buf + i);

                        }
                        //for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *(key_b_sect_xx[4 + j] + (i - 10));}
                        for (i = 10; i < 16; i++) {

                            //*(key_tmp_buf + i) = *(key_b_sect_xx[4 + j] + (i - 10));

                            keyb = key_b_sect_xx[4 + j].getBytes();
                            key_tmp_buf[i] = keyb[i - 10];

                            //  *(key_tmp_buf + i) = *(lpc_key_b_tbl + (4 + j)*6 + (i - 10));


                        }
                        //-----------------------------------------------------------------------//
                        if (blockWrite(KEY_B, keyB, (byte) ((((j + 4) * 4) + 3)), key_tmp_buf) == 1) {
                            return 1;
                        }
                        //-----------------------------------------------------------------------//
                    } else {
                        return 1;
                    }
                }

                return 0;
            //-----------------------------------------------------------------------//
            //-----------------------------------------------------------------------//
            case 2:

                for (j = 6; j < 12; j++) {
                    if (blockRead(KEY_A, keyA, (byte) ((((j + 4) * 4) + 3)), rd_tmp_buf) == 0) {
                        //-----------------------------------------------------------------------//
                        for (i = 0; i < 6; i++) {
                            key_tmp_buf[i] = keyA[i];
                            // *(key_tmp_buf + i) = *(key_a + i);
                        }
                        for (i = 6; i < 10; i++) {
                            key_tmp_buf[i] = rd_tmp_buf[i];
                            // *(key_tmp_buf + i) = *(rd_tmp_buf + i);
                        }
                        //for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *(key_b_sect_xx[4 + j] + (i - 10));}
                        for (i = 10; i < 16; i++) {

                            keyb = key_b_sect_xx[4 + j].getBytes();
                            key_tmp_buf[i] = keyb[i - 10];
                            //*(key_tmp_buf + i) = *(lpc_key_b_tbl + (4 + j)*6 + (i - 10));
                        }
                        //-----------------------------------------------------------------------//
                        if (blockWrite(KEY_B, keyB, (byte) ((((j + 4) * 4) + 3)), key_tmp_buf) == 1) {
                            return 1;
                        }
                        //-----------------------------------------------------------------------//
                    } else {
                        return 1;
                    }
                }

                return 0;
            //-----------------------------------------------------------------------//
            //-----------------------------------------------------------------------//
            default:
                return 1;
            //-----------------------------------------------------------------------//
        }
    }
    //==============================================================================================================//

    //==========================================Finger Template Erase==============================================//

//    unsigned char erase_template(unsigned char t_val)
//    {
//        unsigned char i = 0;
//
//
//        switch(t_val)
//        {
//            case 1:
//                //--------------------------------------------------//
//                if(mad_operation(1)){ return 1; }
//                if(refresh_template_key(1)){ return 2; }
//
//                for(i = 0;i < 6;i++)
//                {if(sector_clean(KEY_B,"543210",(4 + i))){ return 3; }  }
//                //--------------------------------------------------//
//                return 0;
//
//
//            case 2:
//                //--------------------------------------------------//
//                if(mad_operation(3)){ return 1; }
//                if(refresh_template_key(2)){ return 3; }
//
//                for(i = 6;i < 12;i++)
//                {if(sector_clean(KEY_B,"543210",(4 + i))){ return 3; }  }
//                //--------------------------------------------------//
//                return 0;
//
//
//            default:return 4;
//        }
//    }


    public int eraseTemplate(int tVal) {

        int i = 0;
        String strKeyB = "543210";
        byte[] keyB = new byte[6];
        keyB = strKeyB.getBytes();

        switch (tVal) {
            case 1:
                //--------------------------------------------------//
                if (madOperation(1) != 0) {
                    return 1;
                }
                if (refreshTemplateKey(1) != 0) {
                    return 2;
                }

                for (i = 0; i < 6; i++) {
                    if (sectorClean(KEY_B, keyB, (byte) (4 + i)) != 0) {
                        return 3;
                    }
                }
                //--------------------------------------------------//
                return 0;


            case 2:
                //--------------------------------------------------//
                if (madOperation(3) != 0) {
                    return 1;
                }
                if (refreshTemplateKey(2) != 0) {
                    return 3;
                }

                for (i = 6; i < 12; i++) {
                    if (sectorClean(KEY_B, keyB, (byte) (4 + i)) != 0) {
                        return 3;
                    }
                }
                //--------------------------------------------------//
                return 0;


            default:
                return 4;
        }


    }
    //=============================================================================================================//

    //========================================Refresh Template Key=================================================//

//    unsigned char refresh_template_key(unsigned char t_val)
//    {
//        unsigned char  i = 0,j = 0;
//
//
//        switch(t_val)
//        {
//            //-----------------------------------------------------------------------//
//            case 1:
//
//                for (j = 0; j < 6; j++)	 //	12
//                {
//
//                    if(!block_read(KEY_A,"012345",((((j + 4) * 4) + 3)),rd_tmp_buf))
//                    {
//                        //-----------------------------------------------------------------------//
//                        for (i = 0; i < 6;  i++){*(key_tmp_buf + i) = *(key_a + i);}
//                        for (i = 6; i < 10; i++){*(key_tmp_buf + i) = *(rd_tmp_buf + i);}
//                        for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *(key_b + (i - 10));}
//                        //-----------------------------------------------------------------------//
//                        if(block_write(KEY_B,sector_key(KEY_B,(4 + j),key_tmp),((((j + 4) * 4) + 3))
//                                , key_tmp_buf)){ return 1; }
//                        //-----------------------------------------------------------------------//
//                    }
//                    else { return 1;}
//                }
//
//                return 0;
//            //-----------------------------------------------------------------------//
//            //-----------------------------------------------------------------------//
//            case 2:
//
//                for (j = 6; j < 12; j++)
//                {
//
//                    if(!block_read(KEY_A,"012345",((((j + 4) * 4) + 3)),rd_tmp_buf))
//                    {
//                        //-----------------------------------------------------------------------//
//                        for (i = 0; i < 6;  i++){*(key_tmp_buf + i) = *(key_a + i);}
//                        for (i = 6; i < 10; i++){*(key_tmp_buf + i) = *(rd_tmp_buf + i);}
//                        for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *(key_b + (i - 10));}
//                        //-----------------------------------------------------------------------//
//                        if(block_write(KEY_B,sector_key(KEY_B,(4 + j),key_tmp),((((j + 4) * 4) + 3))
//                                , key_tmp_buf)){ return 1; }
//                        //-----------------------------------------------------------------------//
//                    }
//                    else { return 1;}
//
//                }
//
//                return 0;
//            //-----------------------------------------------------------------------//
//            //-----------------------------------------------------------------------//
//            default: return 1;
//            //-----------------------------------------------------------------------//
//        }
//
//    }

    public int refreshTemplateKey(int tVal) {

        int i = 0, j = 0;

        String strKeyA = "012345";
        String strKeyB = "543210";

        byte[] keyA = new byte[6];
        byte[] keyB = new byte[6];

        keyA = strKeyA.getBytes();
        keyB = strKeyB.getBytes();

        switch (tVal) {
            //-----------------------------------------------------------------------//
            case 1:

                for (j = 0; j < 6; j++)     //	12
                {

                    if (blockRead(KEY_A, keyA, (byte) ((((j + 4) * 4) + 3)), rd_tmp_buf) == 0) {
                        //-----------------------------------------------------------------------//
                        for (i = 0; i < 6; i++) {
                            //*(key_tmp_buf + i) =*(key_a + i);
                            key_tmp_buf[i] = keyA[i];
                        }
                        for (i = 6; i < 10; i++) {
                            //*(key_tmp_buf + i) =*(rd_tmp_buf + i);
                            key_tmp_buf[i] = rd_tmp_buf[i];
                        }
                        for (i = 10; i < 16; i++) {
                            // *(key_tmp_buf + i) =*(key_b + (i - 10));
                            key_tmp_buf[i] = keyB[i - 10];
                        }

                        //-----------------------------------------------------------------------//
                        if (blockWrite(KEY_B, sectorKey(KEY_B, (byte) (4 + j), key_tmp), (byte) ((((j + 4) * 4) + 3))
                                , key_tmp_buf) != 0) {
                            return 1;
                        }
                        //-----------------------------------------------------------------------//
                    } else {
                        return 1;
                    }
                }

                return 0;
            //-----------------------------------------------------------------------//
            //-----------------------------------------------------------------------//
            case 2:

                for (j = 6; j < 12; j++) {

                    if (blockRead(KEY_A, keyA, (byte) ((((j + 4) * 4) + 3)), rd_tmp_buf) == 0) {
                        //-----------------------------------------------------------------------//
                        for (i = 0; i < 6; i++) {
                            // *(key_tmp_buf + i) =*(key_a + i);
                            key_tmp_buf[i] = keyA[i];
                        }
                        for (i = 6; i < 10; i++) {
                            // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                            key_tmp_buf[i] = rd_tmp_buf[i];
                        }
                        for (i = 10; i < 16; i++) {
                            //*(key_tmp_buf + i) =*(key_b + (i - 10));
                            key_tmp_buf[i] = keyB[i - 10];
                        }
                        //-----------------------------------------------------------------------//
                        if (blockWrite(KEY_B, sectorKey(KEY_B, (byte) (4 + j), key_tmp), (byte) ((((j + 4) * 4) + 3))
                                , key_tmp_buf) != 0) {
                            //Log.d("TEST", "Error:");
                            return 1;
                        }
                        //-----------------------------------------------------------------------//
                    } else {
                        return 1;
                    }
                }
                return 0;
            //-----------------------------------------------------------------------//
            //-----------------------------------------------------------------------//
            default:
                return 1;
            //-----------------------------------------------------------------------//
        }

    }


    //=============================================================================================================//


    public String bytesToHex(byte[] data) {

        String strHexData = "";
        StringBuilder builder1 = new StringBuilder();
        for (byte b : data) {
            builder1.append(String.format("%02x", b));
        }
        strHexData = builder1.toString().toUpperCase();

        return strHexData;
    }

    //[8][4


    ;

//    {
//        //	Read      Write     Add       Sub   	 code
//        0x7F,0x07,0x88,0xC1, //	Key-A|B   Key-A|B   Key-A|B   Key-A|B	 7F0788C1
//                0x0F,0x07,0x8F,0xC1, //	Key-A|B   never     never     never		 0F078FC1
//                0x78,0x77,0x88,0xC1, //	Key-A|B   Key-B     never     never		 787788C1
//                0x08,0x77,0x88,0xC1, //	Key-A|B   Key-B     Key-B     Key-A|B	 08778FC1
//                0x7F,0x00,0xF8,0xC1, //	Key-A|B   never     never     Key-A|B	 7F00F8C1
//                0x0F,0x00,0xF8,0xC1, //	Key-B     Key-B     never     never		 0F00F8C1
//                0x78,0x70,0xF8,0xC1, //	Key-B     never     never     never		 7870F8C1
//                0x08,0x70,0xFF,0xC1	 //	never     never     never     never		 0870FFC1
//    };


    public int cardInit(boolean tf, byte sectorVal, byte[] cardId) {

        //If tf=true set new Key a,Access Code,Key B
        //If false don't set new Key a,Access Code,Key B

        //Step 4:Start initializing all sectors trailing block from 0 to 15 one by one,change Key-A,Access Code,Key-B
        //Step 5:Read trailing block of sector n where n=0 to 15 with Key-A(Factory Key A[FFFFFFFFFFFF])
        //Step 6:Write Fortuna Key A ,Access Code,Key-B in trailing block with Key-A(Factory Key A[FFFFFFFFFFFF])
        //Access code for all sectors except 1 is 7F0788C1 and  for Sector 1 is 787788C1
        //Step 7:Sector 0 Write/MAD Write.Write Block 1 and Block 2 of Sector 0 with 5501000500000000000000000000000000000000000000000000000000000000 with Key-B(Fortuna Key-B[RESSEC])
        //Step 8:Sector 1 Write/Card Id Write.Write 8 bytes Card Id in Sector 0 Block 0 and rest 8 bytes with default value ie 464F5254554E4120 with Key-B(Fortuna Key-B[CPS ID])
        //Step 9:Sector 2 to 15 Write.Write 48 bytes with zeroes to each sector with Fortuna Key-B of respective sectors.


        if (tf) {

//            tsk_lock();
//            i = block_read(KEY_A, (char *)(temp_fat_buf+248), ((sn * 4) + 3), rd_tmp_buf);
//            tsk_unlock();
//            if(i)
//            {
//                display("?RA");			// +++++for testing purpose+++++
//                srl0_put_byte(sn);		// +++++for testing purpose+++++
//                srl0_put_byte(i);		// +++++for testing purpose+++++
//                return __FALSE;
//            }
//            //Now Modify 'Key-A', 'Access Code' & 'Key-B'
//            r4 = key_a_sect_xx[sn];
//            for (i = 0; i < 6;  i++){*(key_tmp_buf + i) = *r4++;}		//New 'Key-A'
//            r4 = access_code[0];
//            if(sn == 1){r4 = access_code[2];}	//for Sector-1 Access-Code is Different
//            for (i = 6; i < 10; i++){*(key_tmp_buf + i) = *r4++;}		//New Access Code
//            r4 = key_b_dflt[sn];
//            for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *r4++;}		//New 'Key-B'
//            for(a2 = 0xFFFF; a2 != 0; a2--){;}		//a Simple Delay
//            i = sc_blk_wr(KEY_A, (temp_fat_buf+248), ((sn * 4) + 3), (U8 *)key_tmp_buf);
//            //i = block_write(KEY_A, (char *)(temp_fat_buf+248), ((sn * 4) + 3), key_tmp_buf);
//            if(i)
//            {
//                display("?WA");			// +++++for testing purpose+++++
//                srl0_put_byte(sn);		// +++++for testing purpose+++++
//                srl0_put_byte(i);			// +++++for testing purpose+++++
//                return __FALSE;
//            }
//            //************ New 'Key-A', New 'Access Code' & New 'Key-B' has been SET ************

            int i = 0;
            byte[] factKeyA = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            if (blockRead(KEY_A, factKeyA, (byte) ((sectorVal * 4) + 3), key_tmp_buf) == 0) {

                byte[] keya = new byte[6];
                byte[] keyb = new byte[6];
                keya = key_a_sect_xx[sectorVal].getBytes();
                keyb = key_b_dflt[sectorVal].getBytes();


                //Access code for sector 1
                //787788C1
                byte[] accessCode1 = accessCodes[2];

                //Access code for all sectors 0,2,3,4,5,6,7,8,9,10,11,12,13,14,15
                //7F0788C1
                byte[] accessCode2 = accessCodes[0];


                for (i = 0; i < 6; i++) {
                    // *(key_tmp_buf + i) =*(key_a + i);
                    key_tmp_buf[i] = keya[i];
                }

                if (sectorVal == 0x01) {
                    int z = 0;
                    for (i = 6; i < 10; i++) {
                        // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                        key_tmp_buf[i] = accessCode1[z++];
                    }
                } else {
                    int z = 0;
                    for (i = 6; i < 10; i++) {
                        // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                        key_tmp_buf[i] = accessCode2[z++];
                    }
                }

                for (i = 10; i < 16; i++) {
                    //*(key_tmp_buf + i) =*(key_b + (i - 10));
                    key_tmp_buf[i] = keyb[i - 10];
                }

                if (blockWrite(KEY_A, factKeyA, (byte) ((sectorVal * 4) + 3), key_tmp_buf) != 0) {
                    return 1;
                }

                switch (sectorVal) {
                    case 0:

                        //for Sector-0......First 16-bytes should remain UnChanged

                        byte[] mad0 = new byte[]{0x55, 0x01, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        byte[] mad1 = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

                        if (blockWrite(KEY_B, keyb, (byte) 0x01, mad0) != 0) {
                            return 1;
                        }

                        if (blockWrite(KEY_B, keyb, (byte) 0x02, mad1) != 0) {
                            return 1;
                        }

                        break;


                    case 1:

                        byte[] block4 = new byte[16];
                        int l;
                        for (l = 0; l < 8; l++) {
                            block4[l] = cardId[l];
                        }
                        for (l = 8; l < 16; l++) {
                            block4[l] = sectr1_dflt_val[l];
                        }
                        if (blockWrite(KEY_B, keyb, (byte) 0x04, block4) != 0) {
                            return 1;
                        }

                        byte[] block5 = new byte[]{(byte) 0x11, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xAA, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
                        byte[] block6 = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};


                        if (blockWrite(KEY_B, keyb, (byte) 0x05, block5) != 0) {
                            return 1;
                        }

                        if (blockWrite(KEY_B, keyb, (byte) 0x06, block6) != 0) {
                            return 1;
                        }

                        break;

                    default:

                        //For other Sectors
                        byte[] zeroBytes = new byte[48];

                        for (int j = 0; j < 48; j++) {
                            zeroBytes[j] = 0x00;
                        }

                        if (sectorWrite(KEY_B, keyb, sectorVal, zeroBytes) != 0) {
                            return 1;
                        }

                        break;
                }


            } else {
                return 1;
            }
        }
        return 0;
    }


    public int cardRefresh(byte sectorVal, byte[] cardId) {

//        U8 *r5,*r6;
//        char const *r4;
//        U8 i;
//        U16 a2;
//        BIT x = __FALSE;
//
//        r4 = key_b_dflt[sv];
//        r6 = temp_fat_buf;
//        for(i = 6; i != 0; i--){*r6++ = *r4++;}
//        //
//        switch(sv)
//        {
//            case 0:
//                //for Sector-0
//                r6 = gp_data_arr2;
//                //Fill with [ 55,01,00,05,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00 ]
//                *r6++ = 0x55;
//                *r6++ = 0x01;
//                *r6++ = 0x00;
//                *r6++ = 0x05;
//                for(i = 28; i != 0; i--){*r6++ = 0x00;}
//                i = sc_blk_wr(KEY_B, temp_fat_buf, 1, gp_data_arr2);
//                if(i == 0)
//                {
//                    srl0_put_char('d');		// +++++for testing purpose+++++
//                    for(a2 = 0xFFF; a2 != 0; a2--){;}		//a Simple Delay
//                    i = sc_blk_wr(KEY_B, temp_fat_buf, 2, (gp_data_arr2+16));
//                }
//                break;
//
//            case 1:
//                r5 = gp_data_arr2+240;
//                if(*r5 != 0xFF)
//            {
//                i = sc_blk_rd(KEY_B, temp_fat_buf, 4, gp_data_arr2);
//                if(i == 0)
//                {
//                    r6 = gp_data_arr2;
//                    for(i = ID_DIGIT; i != 0; i--){*r6++ = *r5++;}
//                    for(a2 = 0xFFF; a2 != 0; a2--){;}		//a Simple Delay
//                    i = sc_blk_wr(KEY_B, temp_fat_buf, 4, gp_data_arr2);
//                }
//            }
//            break;
//
//
//            case 3:
//                //for Sector-1,2,3
//                i = 0;
//                break;
//
//            default:
//                //For other Sectors(Sector-4 TO Sector-15)...First Try to Read 4th-Block of the Sector('sv') with default KEY-B
//                i = sc_blk_rd(KEY_B, temp_fat_buf, ((sv * 4) + 3), gp_data_arr2);
//                if(i)
//                {
//                    srl0_put_char('a');		// +++++for testing purpose+++++
//                    //Now Try to Read 4th-Block of the Sector('sv') with Template KEY-B
//                    for(a2 = 0xFFF; a2 != 0; a2--){;}		//a Simple Delay
//                    i = sc_blk_rd(KEY_B, (lpc_key_b_tbl + sv * 6), ((sv * 4) + 3), gp_data_arr2);
//                    if(i)
//                    {
//                        if(sv == 2)	//For Sector-2 Only
//                        {
//                            for(a2 = 0xFFF; a2 != 0; a2--){;}		//a Simple Delay
//                            i = sc_blk_rd(KEY_B, "PERINF", ((sv * 4) + 3), gp_data_arr2);
//                            x = __TRUE;
//                        }
//                    }
//                    if(i == 0)
//                    {
//                        srl0_put_char('b');		// +++++for testing purpose+++++
//                        //The Block is Successfully Read with Template KEY-B....Change KEY-B First
//                        for (i = 0; i < 6;  i++){*(gp_data_arr2 + i) = *(key_a + i);}
//                        r6 = temp_fat_buf;
//                        for (i = 10;i < 16; i++){*(gp_data_arr2 + i) = *r6++;}
//                        for(a2 = 0xFFFF; a2 != 0; a2--){;}		//a Simple Delay
//                        if(x){i = sc_blk_wr(KEY_B, "PERINF", ((sv * 4) + 3), gp_data_arr2);}
//                        else{i = sc_blk_wr(KEY_B, (lpc_key_b_tbl + sv * 6), ((sv * 4) + 3), gp_data_arr2);}
//                        if(i == 0)
//                        {
//                            srl0_put_char('c');		// +++++for testing purpose+++++
//                            //KEY-B is Successfully Changed
//                            r6 = gp_data_arr2;
//                            for(i = 48; i != 0; i--){*r6++ = 0x00;}
//                            for(a2 = 0xFFFF; a2 != 0; a2--){;}		//a Simple Delay
//                            i = sc_sect_wr(KEY_B, temp_fat_buf, sv, gp_data_arr2);
//                        }
//                    }
//                }
//                break;
//        }
//        //
//        if(i){return __FALSE;}
//        srl0_put_char('%');		// +++++for testing purpose+++++
//        for(a2 = 0xFFFF; a2 != 0; a2--){;}		//a Simple Delay
//        return __TRUE;

        byte[] keya = new byte[6];
        byte[] keybdflt = new byte[6];
        byte[] keyb = new byte[6];
        keya = key_a_sect_xx[sectorVal].getBytes();
        keybdflt = key_b_dflt[sectorVal].getBytes();
        keyb = key_b_sect_xx[sectorVal].getBytes();


        switch (sectorVal) {

            case 0:

                //for Sector-0......First 16-bytes should remain UnChanged

                byte[] mad0 = new byte[]{0x55, 0x01, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                byte[] mad1 = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

                if (blockWrite(KEY_B, keyb, (byte) 0x01, mad0) != 0) {
                    return 1;
                }

                if (blockWrite(KEY_B, keyb, (byte) 0x02, mad1) != 0) {
                    return 1;
                }
                break;

            case 1:

                byte[] block4 = new byte[16];
                int l;
                for (l = 0; l < 8; l++) {
                    block4[l] = cardId[l];
                }
                for (l = 8; l < 16; l++) {
                    block4[l] = sectr1_dflt_val[l];
                }
                if (blockWrite(KEY_B, keyb, (byte) 0x04, block4) != 0) {
                    return 1;
                }

                byte[] block5 = new byte[]{(byte) 0x11, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0xAA, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
                byte[] block6 = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

                if (blockWrite(KEY_B, keyb, (byte) 0x05, block5) != 0) {
                    return 1;
                }

                if (blockWrite(KEY_B, keyb, (byte) 0x06, block6) != 0) {
                    return 1;
                }

                break;

            case 3:
                break;

            default:

                boolean x = false;
                int i = blockRead(KEY_B, keybdflt, (byte) ((sectorVal * 4) + 3), rd_tmp_buf);
                if (i!=0) {
                    i = blockRead(KEY_B, keyb, (byte) ((sectorVal * 4) + 3), rd_tmp_buf);
                    if (i != 0) {

                        if (sectorVal == 0x02) {
                            String strSector2KeyB = "PERINF";
                            byte[] key = new byte[6];
                            key = strSector2KeyB.getBytes();
                            i = blockRead(KEY_B, key, (byte) ((sectorVal * 4) + 3), rd_tmp_buf);
                            x = true;
                        }
                    }

                    if (i == 0) {

                        //The Block is Successfully Read with Template KEY-B....Change KEY-B First

                        //Access code for sector 1
                        //787788C1
                        byte[] accessCode1 = accessCodes[2];

                        //Access code for all sectors 0,2,3,4,5,6,7,8,9,10,11,12,13,14,15
                        //7F0788C1
                        byte[] accessCode2 = accessCodes[0];

                        for (i = 0; i < 6; i++) {
                            // *(key_tmp_buf + i) =*(key_a + i);
                            key_tmp_buf[i] = keya[i];
                        }

                        if (sectorVal == 0x01) {
                            int z = 0;
                            for (i = 6; i < 10; i++) {
                                // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                                key_tmp_buf[i] = accessCode1[z++];
                            }
                        } else {
                            int z = 0;
                            for (i = 6; i < 10; i++) {
                                // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                                key_tmp_buf[i] = accessCode2[z++];
                            }
                        }

                        for (i = 10; i < 16; i++) {
                            //*(key_tmp_buf + i) =*(key_b + (i - 10));
                            key_tmp_buf[i] = keybdflt[i - 10];
                        }


                        if (x) {
                            // i = sc_blk_wr(KEY_B, "PERINF", ((sv * 4) + 3), gp_data_arr2);
                            String strSector2KeyB = "PERINF";
                            byte[] key = new byte[6];
                            key = strSector2KeyB.getBytes();
                            i = blockWrite(KEY_B, key, (byte) ((sectorVal * 4) + 3), key_tmp_buf);
                        } else {
                            //i = sc_blk_wr(KEY_B, (lpc_key_b_tbl + sv * 6), ((sv * 4) + 3), gp_data_arr2);
                            i = blockWrite(KEY_B, keyb, (byte) ((sectorVal * 4) + 3), key_tmp_buf);
                        }

                        if (i == 0) {

                            //For other Sectors
                            byte[] zeroBytes = new byte[48];

                            for (int j = 0; j < 48; j++) {
                                zeroBytes[j] = 0x00;
                            }

                            if (sectorWrite(KEY_B, keybdflt, sectorVal, zeroBytes) != 0) {
                                return 1;
                            }
                        }
                    }
                }

                break;
        }
        return 0;
    }

    public int fortunaCardToFactoryCard(byte sectorVal) {

        //If tf=true set new Key a,Access Code,Key B
        //If false don't set new Key a,Access Code,Key B

        //Step 4:Start initializing all sectors trailing block from 0 to 15 one by one,change Key-A,Access Code,Key-B
        //Step 5:Read trailing block of sector n where n=0 to 15 with Key-A(Factory Key A[FFFFFFFFFFFF])
        //Step 6:Write Fortuna Key A ,Access Code,Key-B in trailing block with Key-A(Factory Key A[FFFFFFFFFFFF])
        //Access code for all sectors except 1 is 7F0788C1 and  for Sector 1 is 787788C1
        //Step 7:Sector 0 Write/MAD Write.Write Block 1 and Block 2 of Sector 0 with 5501000500000000000000000000000000000000000000000000000000000000 with Key-B(Fortuna Key-B[RESSEC])
        //Step 8:Sector 1 Write/Card Id Write.Write 8 bytes Card Id in Sector 0 Block 0 and rest 8 bytes with default value ie 464F5254554E4120 with Key-B(Fortuna Key-B[CPS ID])
        //Step 9:Sector 2 to 15 Write.Write 48 bytes with zeroes to each sector with Fortuna Key-B of respective sectors.


//            tsk_lock();
//            i = block_read(KEY_A, (char *)(temp_fat_buf+248), ((sn * 4) + 3), rd_tmp_buf);
//            tsk_unlock();
//            if(i)
//            {
//                display("?RA");			// +++++for testing purpose+++++
//                srl0_put_byte(sn);		// +++++for testing purpose+++++
//                srl0_put_byte(i);		// +++++for testing purpose+++++
//                return __FALSE;
//            }
//            //Now Modify 'Key-A', 'Access Code' & 'Key-B'
//            r4 = key_a_sect_xx[sn];
//            for (i = 0; i < 6;  i++){*(key_tmp_buf + i) = *r4++;}		//New 'Key-A'
//            r4 = access_code[0];
//            if(sn == 1){r4 = access_code[2];}	//for Sector-1 Access-Code is Different
//            for (i = 6; i < 10; i++){*(key_tmp_buf + i) = *r4++;}		//New Access Code
//            r4 = key_b_dflt[sn];
//            for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *r4++;}		//New 'Key-B'
//            for(a2 = 0xFFFF; a2 != 0; a2--){;}		//a Simple Delay
//            i = sc_blk_wr(KEY_A, (temp_fat_buf+248), ((sn * 4) + 3), (U8 *)key_tmp_buf);
//            //i = block_write(KEY_A, (char *)(temp_fat_buf+248), ((sn * 4) + 3), key_tmp_buf);
//            if(i)
//            {
//                display("?WA");			// +++++for testing purpose+++++
//                srl0_put_byte(sn);		// +++++for testing purpose+++++
//                srl0_put_byte(i);			// +++++for testing purpose+++++
//                return __FALSE;
//            }
//            //************ New 'Key-A', New 'Access Code' & New 'Key-B' has been SET ************

        int i = 0;

        byte[] key_a = new byte[6];
        byte[] key_b = new byte[6];

        key_a = key_a_sect_xx[sectorVal].getBytes();
        key_b = key_b_sect_xx[sectorVal].getBytes();

        byte[] keya = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        byte[] keyb = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        byte[] accesscode = new byte[]{(byte) 0xFF, (byte) 0x07, (byte) 0x80, (byte) 0x69};

        int k;
        k = blockRead(KEY_A, key_a, (byte) ((sectorVal * 4) + 3), rd_tmp_buf);

        if (k == 0) {

            for (i = 0; i < 6; i++) {
                // *(key_tmp_buf + i) =*(key_a + i);
                key_tmp_buf[i] = keya[i];
            }

            int z = 0;
            for (i = 6; i < 10; i++) {
                // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                key_tmp_buf[i] = accesscode[z++];
            }

            for (i = 10; i < 16; i++) {
                //*(key_tmp_buf + i) =*(key_b + (i - 10));
                key_tmp_buf[i] = keyb[i - 10];
            }

            z = blockWrite(KEY_B, key_b, (byte) ((sectorVal * 4) + 3), key_tmp_buf);

            if (z != 0) {
                return 1;
            }

            switch (sectorVal) {

                case 0:

                    byte[] zeroBytes0 = new byte[16];

                    for (int j = 0; j < 16; j++) {
                        zeroBytes0[j] = 0x00;
                    }

                    int x;
                    x = blockWrite(KEY_A, keya, (byte) 0X01, zeroBytes0);
                    if (x == 0) {
                        x = blockWrite(KEY_A, keya, (byte) 0X02, zeroBytes0);
                    } else {
                        return 1;
                    }

                    break;

                default:

                    byte[] zeroBytesDef = new byte[48];

                    for (int j = 0; j < 48; j++) {
                        zeroBytesDef[j] = 0x00;
                    }

                    if (sectorWrite(KEY_A, keya, sectorVal, zeroBytesDef) != 0) {
                        return 1;
                    }

                    break;
            }
        } else {
            return 1;
        }

        return 0;
    }

    public int initCardToFactoryCard(byte sectorVal){


        //If tf=true set new Key a,Access Code,Key B
        //If false don't set new Key a,Access Code,Key B

        //Step 4:Start initializing all sectors trailing block from 0 to 15 one by one,change Key-A,Access Code,Key-B
        //Step 5:Read trailing block of sector n where n=0 to 15 with Key-A(Factory Key A[FFFFFFFFFFFF])
        //Step 6:Write Fortuna Key A ,Access Code,Key-B in trailing block with Key-A(Factory Key A[FFFFFFFFFFFF])
        //Access code for all sectors except 1 is 7F0788C1 and  for Sector 1 is 787788C1
        //Step 7:Sector 0 Write/MAD Write.Write Block 1 and Block 2 of Sector 0 with 5501000500000000000000000000000000000000000000000000000000000000 with Key-B(Fortuna Key-B[RESSEC])
        //Step 8:Sector 1 Write/Card Id Write.Write 8 bytes Card Id in Sector 0 Block 0 and rest 8 bytes with default value ie 464F5254554E4120 with Key-B(Fortuna Key-B[CPS ID])
        //Step 9:Sector 2 to 15 Write.Write 48 bytes with zeroes to each sector with Fortuna Key-B of respective sectors.


//            tsk_lock();
//            i = block_read(KEY_A, (char *)(temp_fat_buf+248), ((sn * 4) + 3), rd_tmp_buf);
//            tsk_unlock();
//            if(i)
//            {
//                display("?RA");			// +++++for testing purpose+++++
//                srl0_put_byte(sn);		// +++++for testing purpose+++++
//                srl0_put_byte(i);		// +++++for testing purpose+++++
//                return __FALSE;
//            }
//            //Now Modify 'Key-A', 'Access Code' & 'Key-B'
//            r4 = key_a_sect_xx[sn];
//            for (i = 0; i < 6;  i++){*(key_tmp_buf + i) = *r4++;}		//New 'Key-A'
//            r4 = access_code[0];
//            if(sn == 1){r4 = access_code[2];}	//for Sector-1 Access-Code is Different
//            for (i = 6; i < 10; i++){*(key_tmp_buf + i) = *r4++;}		//New Access Code
//            r4 = key_b_dflt[sn];
//            for (i = 10;i < 16; i++){*(key_tmp_buf + i) = *r4++;}		//New 'Key-B'
//            for(a2 = 0xFFFF; a2 != 0; a2--){;}		//a Simple Delay
//            i = sc_blk_wr(KEY_A, (temp_fat_buf+248), ((sn * 4) + 3), (U8 *)key_tmp_buf);
//            //i = block_write(KEY_A, (char *)(temp_fat_buf+248), ((sn * 4) + 3), key_tmp_buf);
//            if(i)
//            {
//                display("?WA");			// +++++for testing purpose+++++
//                srl0_put_byte(sn);		// +++++for testing purpose+++++
//                srl0_put_byte(i);			// +++++for testing purpose+++++
//                return __FALSE;
//            }
//            //************ New 'Key-A', New 'Access Code' & New 'Key-B' has been SET ************

        int i = 0;

        byte[] key_a = new byte[6];
        byte[] key_b = new byte[6];

        key_a = key_a_sect_xx[sectorVal].getBytes();
        key_b = key_b_dflt[sectorVal].getBytes();

        byte[] keya = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        byte[] keyb = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        byte[] accesscode = new byte[]{(byte) 0xFF, (byte) 0x07, (byte) 0x80, (byte) 0x69};

        int k;
        k = blockRead(KEY_A, key_a, (byte) ((sectorVal * 4) + 3), rd_tmp_buf);

        if (k == 0) {

            for (i = 0; i < 6; i++) {
                // *(key_tmp_buf + i) =*(key_a + i);
                key_tmp_buf[i] = keya[i];
            }

            int z = 0;
            for (i = 6; i < 10; i++) {
                // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                key_tmp_buf[i] = accesscode[z++];
            }

            for (i = 10; i < 16; i++) {
                //*(key_tmp_buf + i) =*(key_b + (i - 10));
                key_tmp_buf[i] = keyb[i - 10];
            }

            z = blockWrite(KEY_B, key_b, (byte) ((sectorVal * 4) + 3), key_tmp_buf);

            if (z != 0) {
                return 1;
            }

            switch (sectorVal) {

                case 0:

                    byte[] zeroBytes0 = new byte[16];

                    for (int j = 0; j < 16; j++) {
                        zeroBytes0[j] = 0x00;
                    }

                    int x;
                    x = blockWrite(KEY_A, keya, (byte) 0X01, zeroBytes0);
                    if (x == 0) {
                        x = blockWrite(KEY_A, keya, (byte) 0X02, zeroBytes0);
                    } else {
                        return 1;
                    }

                    break;

                default:

                    byte[] zeroBytesDef = new byte[48];

                    for (int j = 0; j < 48; j++) {
                        zeroBytesDef[j] = 0x00;
                    }

                    if (sectorWrite(KEY_A, keya, sectorVal, zeroBytesDef) != 0) {
                        return 1;
                    }

                    break;
            }
        } else {
            return 1;
        }

        return 0;

    }

    //================Test Function==========//

    public int testblockWrite(int blockVal){

        byte[] keyaf = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        byte[] keybf = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

        byte[] keyai = new byte[]{(byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35};
        byte[] keybi = new byte[]{(byte) 0x35, (byte) 0x34, (byte) 0x33, (byte) 0x32, (byte) 0x31, (byte) 0x30};


        byte[] accesscode = new byte[]{(byte) 0xFF, (byte) 0x07, (byte) 0x80, (byte) 0x69};
        //7F0788C1

       // byte[] accesscode = new byte[]{(byte) 0x7F, (byte) 0x07, (byte) 0x88, (byte) 0xC1};

        int i;
        int k;
        k = blockRead(KEY_A, keyai, (byte) blockVal, rd_tmp_buf);

        if (k == 0) {

            for (i = 0; i < 6; i++) {
                // *(key_tmp_buf + i) =*(key_a + i);
                key_tmp_buf[i] = keyaf[i];
            }

            int z = 0;
            for (i = 6; i < 10; i++) {
                // *(key_tmp_buf + i) =*(rd_tmp_buf + i);
                key_tmp_buf[i] = accesscode[z++];
            }

            for (i = 10; i < 16; i++) {
                //*(key_tmp_buf + i) =*(key_b + (i - 10));
                key_tmp_buf[i] = keybf[i - 10];
            }

            //for other cards
            z = blockWrite(KEY_B, keybi, (byte) blockVal, key_tmp_buf);

            //for factory cards
            //z = blockWrite(KEY_A, keyai, (byte) blockVal, key_tmp_buf);


            if(z!=0){
                return 1;
            }
        }else{
            return 1;
        }

        return 0;
    }




    //======================================//




}
