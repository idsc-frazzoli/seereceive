#include <iostream>
#include <stdlib.h>
#include <termios.h>
#include <fcntl.h>
#include <pthread.h>
#include <stdio.h>
#include <sys/io.h>
#include <unistd.h>

using namespace std;

int running = 1;
int tty_fid;

void* serial_tx_thread(void *ptr) {
  while (running) {
    int msg = cin.get();
    running = msg != -1; // assumption that EOF == -1 (see libio.h)
    if (running) {
      char chr = msg;
      ssize_t cnt = write(tty_fid, &chr, 1);
      running = cnt == 1;
    }
  }
  return 0;
}

#define RX_BUFFER_SIZE 32

// compile with
// g++ javacom.cpp -o javacom -lpthread
// example how to run
// ./javacom /dev/ttyUSB0 115200
int main(int argc, char** argv) {
  if (2 < argc) {
    tty_fid = open(argv[1], O_RDWR | O_NOCTTY | O_NDELAY);
    if (tty_fid == -1){
      std::cout << "javacom: cannot read from your port, stopping execution.\n";
      return 1;      
    }
    char command[500];
    sprintf(command,"stty raw -crtscts -echo ispeed %s ospeed %s -F %s",argv[2],argv[2],argv[1]);
    system(command);
    fcntl(tty_fid, F_SETFL, O_NONBLOCK);
    pthread_t thread;
    void* arg = 0;
    int ret = pthread_create(&thread, NULL, serial_tx_thread, arg);
    // ---
    char msg[RX_BUFFER_SIZE];
    while (running) {
      ssize_t tty_read = read(tty_fid, msg, RX_BUFFER_SIZE);
      if (0 < tty_read) {
        for (int c0 = 0; c0 < tty_read; ++c0)
          cout << msg[c0];
        cout.flush();
      }
      usleep(2000); // in virtual machine this results in 2-3% cpu 
    }
    // pthread_join(thread, NULL);
    close(tty_fid);
  }
  return 0;
}
