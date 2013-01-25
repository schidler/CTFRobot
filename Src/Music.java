/**
 * 
 * @author Vincent Petrella.
 * 	
 * Just For Fun, we sometime need to take a break in the lab.
 * 
 */

import lejos.nxt.Sound;

public class Music extends Thread
{
	private int beatPerMinute, volume;
	private Notes[][] score;
	
	public Music(int bpm, Notes[][] score, int volume)
	{
		this.volume = volume;
		beatPerMinute = bpm;
		this.score = score;
	}

	public enum Notes
	{
		Do(32.7),
		ReB(34.65),
		Re(36.71),
		MiB(38.89),
		Mi(41.20),
		Fa(43.65),
		SolB(46.25),
		Sol(49.00),
		LaB(51.91),
		La(55.00),
		SiB(58.27),
		Si(61.74),
		Pause(0);

		public double frequency;
		public int tone=3;

		Notes(double frequency)
		{
			this.frequency = (frequency * Math.pow(2, this.tone));
		}

		public void Tone(int tone)
		{
			this.tone = tone;
			this.frequency = (frequency * Math.pow(2, this.tone));
		}
	}

	public void run()
	{
		for( int phrase=0; phrase<score.length; phrase++ )
		{
			for( int note=0; note < score[phrase].length ; note++ )
			{
				Notes currentNote = score[phrase][note];
				PlayNote(currentNote);
				try { Thread.sleep((int)((60.0/beatPerMinute)  * 1000)); } catch(Exception e){}
			}
		}
	}

	void PlayNote(Notes note)
	{
		Sound.playTone((int)note.frequency,(int)((60.0/beatPerMinute)  * 1000),volume);
	}

}
