//avrdude -p m88 -P usb -c avrispmkii -Uflash:w:BlinkLED.hex
//https://sites.google.com/site/qeewiki/books/avr-guide/digital-inputs
//

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include "atmega-adc.h"

#define F_CPU 1000000UL

#define DHT_FLOAT 1


int serial_putchar(char, FILE *);
int serial_getchar(FILE *);
static FILE serial_stream = FDEV_SETUP_STREAM (serial_putchar, serial_getchar, _FDEV_SETUP_RW);

void init_serial(void);
void do_high_low(void);
void user_io(void);



/* this is the high_low server running on serial on an avr */
/* it sets up the serial communication for 1200 baud */
/* note that the random number generator will always generate the same sequence */
int main(void)
{
	init_serial();
	_delay_ms(2000);
	DDRB |= (1<<DDB0);
	
	DDRB &= ~(1 << DDB1); // Clear the PB1 pin and make an input
	PORTB |= (1 << PORTB1); // turn On the Pull-up


	//DDRB = 0xff;
	//PORTB = 0;
	uint16_t light_level;


	while(1)
	{
		user_io();
	}
	return 0;
}

/* Initializes AVR USART for 1200 baud (assuming 1MHz clock) */
/* 1MHz/(16*(51+1)) = 1202 about 0.2% error                  */
void init_serial(void)
{
	UBRR0H=0;
	UBRR0L=12; // 1200 BAUD FOR 1MHZ SYSTEM CLOCK
	UCSR0A= 1<<U2X0;
	UCSR0C= (1<<USBS0)|(3<<UCSZ00) ;  // 8 BIT NO PARITY 2 STOP
	UCSR0B=(1<<RXEN0)|(1<<TXEN0)  ; //ENABLE TX AND RX ALSO 8 BIT
	stdin=&serial_stream;
	stdout=&serial_stream;

}

void user_io(void){
	
	FILE *fp, *fpr;
	int usr_input;
	fp=stdout;
	fpr=stdin;

	usr_input = -1;
	sei();


		
	while(1)
	{   	
		usr_input = serial_getchar(fpr);
		//Turn on LED!
		//98 == b
		if(usr_input == 98)
		{
			//PB0 Pin
			PORTB |= (1 << PB0);
		    printf("LED is now on...\n");
		}//97 == a
		else if (usr_input == 97)
		{
			PORTB &= ~(1 << PB0); 
		    printf("LED is now off...\n");
		}
		fflush(fp);
	}
	fflush(fp);
	fclose(fp);
	fclose(fpr);

	
}


//simplest possible putchar, waits until UDR is empty and puts character
int serial_putchar(char val, FILE * fp)
{
	while((UCSR0A&(1<<UDRE0)) == 0); //wait until empty
	UDR0 = val;
	return 0;
}

//simplest possible getchar, waits until a char is available and reads it
//note:1) it is a blocking read (will wait forever for a char)
//note:2) if multiple characters come in and are not read, they will be lost
int serial_getchar(FILE * fp)
{
	uint16_t light_level;
	light_level = 0;
	while( (UCSR0A&(1<<RXC0) ) == 0)
	{
		
		if( (PINB & (1<<PINB1)) == 0)
		{
			//PB1 Pin
			while((PINB & (1<<PINB1)) == 0);
			printf("fart\n");
			fprintf(fp,"\n");
		}
		
		light_level = adc_read(ADC_PRESCALER_128, ADC_VREF_AVCC, 0);
		printf("Light lvl: %d\n", light_level);
		if( light_level > 1020){
			while(light_level >1020){
				light_level = adc_read(ADC_PRESCALER_128, ADC_VREF_AVCC, 0);
			//	printf("Light lvl: %d\n", light_level);
			}
			_delay_ms(700);
			printf("flashlight\n");
			_delay_ms(500);
			
		} 
		
	} //WAIT FOR CHAR
	return UDR0;
}
