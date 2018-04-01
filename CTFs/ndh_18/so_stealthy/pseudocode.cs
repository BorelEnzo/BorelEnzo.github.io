class AingeiRai5HahfeiThe2 extends Form{

    private Button Aof0roo2eej3ahSh1eis
    private TextBox ta4vo2Ahk5yaep2oShuu
    private Label Xahhu2ieSh5ieFohPiGh
    private string Tai8Aip0ua3ULi6zo1je
    private int32[] az5nieghahj0Iekah0ph

    public AingeiRai5HahfeiThe2()   {
		az5nieghahj0Iekah0ph = [0x15,0x5B,0x14,0x00,0x7E,0x00,0x3D,0x18,0x02,0x52,0x07,0x11,0x58,0x16,0x12,0x15,0x72,0x75,0x0F,0x50,0x3B,0x18]
    }

    public void Aa6bi4uidan4shahSee9 (string ain7aek2Thae3Boh7ohh){
		Tai8Aip0ua3ULi6zo1je = ain7aek2Thae3Boh7ohh.Substring(ain7aek2Thae3Boh7ohh.get_Length() - 0x16)
    }
	
	public void Joh8achoo1aepahjeiy9 () {
		Application.Run()
    }

	//don't really care, it only builds the GUI
    private void Jojei5ahyah2yah5laeK (){}

	private bool MeeBish0iotho9biBuJi (string magicWord){
		for (i = 0; i < Tai8Aip0ua3ULi6zo1je.get_Length(); i++){
			if (ord(az5nieghahj0Iekah0ph[i]) != ord(Tai8Aip0ua3ULi6zo1je[i])^ord(magicWord[i])){
				return false
			}
			return true
    }

	private void Eey4jie0raer7Miiphuo (object ahrah0iwoChohs2dai4a, EventArgs ahH5eedeiYohquei8goo)  {
		if (System.Diagnostics.Debugger::get_IsAttached()){
			print "Don't try your dirty tricks on me!"
		}
		else if (ta4vo2Ahk5yaep2oShuu.get_Text().get_Length()){
			print "You must fill this field!"
		}
		else if(MeeBish0iotho9biBuJi(ta4vo2Ahk5yaep2oShuu.get_Text())){
			print "SUCCESS !\nSubmit NDH{" ta4vo2Ahk5yaep2oShuu.get_Text()+ "} to validate."
		}
		else{
			print "YOU DIDN'T SAY THE MAGIC WORD !!!"
		}
  }



