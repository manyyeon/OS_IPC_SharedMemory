package com.company;

import javax.swing.*;
import java.awt.*;

// 공유 메모리
class SharedMemory{
    MyFrame myFrame; // 화면
    int in = 0; // buffer에 producer가 식을 넣을 공간 인덱스(초록색)
    int out = 0; // buffer에서 consumer가 식을 가져갈 공간 인덱스(빨간색)

    JPanel [] bufferOuterBox; // bufferBox를 담는 패널
    JLabel [] bufferBox; // 버퍼 공간
    String [][] buffer; // 버퍼
    int equationNumber; // 계산할 사칙연산 개수
    int bufferSize; // 버퍼 사이즈

    String [] consumingProblem; // 소비자 스레드에게 전달해 줄 사칙연산

    String showBufferProblem = ""; // 현재 버퍼에 들어온 사칙연산을 띄워주기 위한 문자열 변수

    // 생성자에서 버퍼, 사칙연산 개수 초기화
    SharedMemory(MyFrame myFrame, int equationNumber, int bufferSize, JLabel [] bufferBox, JPanel [] bufferOuterBox){
        // 화면 가져오기
        this.myFrame = myFrame;
        // 계산할 사칙연산 개수 가져오기
        this.equationNumber = equationNumber;
        // 버퍼 사이즈 가져오기
        this.bufferSize = bufferSize;
        // 버퍼 공간 가져오기
        this.bufferBox = bufferBox;
        this.bufferOuterBox = bufferOuterBox;
        buffer = new String[this.bufferSize][];
    }

    // 생산자 스레드가 버퍼에 접근할 수 있게 해주는 함수
    // 생산자 스레드가 생성한 사칙연산을 매개변수로 받아와서 버퍼에 넣어줌
    // 버퍼가 꽉 찼으면 공간이 생길 때까지 대기함
    synchronized void produce(String [] problem){
        // 버퍼가 꽉 찼을 때
        if((in+1) % bufferSize == out){
            try{
                // 버퍼에 공간이 생길 때까지 기다리기
                wait();
            }
            catch(InterruptedException e){
                return;
            }
        }
        // 버퍼의 in이 가리키는 인덱스에 생산자 스레드가 만든 랜덤 사칙연산 저장
        // 랜덤 사칙연산을 화면에도 표시
        buffer[in] = new String[problem.length]; // 사칙연산 길이만큼 공간 할당
        showBufferProblem = ""; // 화면에 표시해줄 사칙연산
        for(int i=0; i<problem.length; i++){
            buffer[in][i] = problem[i]; // 버퍼에 사칙연산 삽입
            showBufferProblem += problem[i]; // 화면에 표시해줄 문자열에 사칙연산 저장
        }

        // 사칙연산 문자열을 화면에 표시
        bufferBox[in].setText(showBufferProblem);

        bufferOuterBox[in].setBackground(new Color(237, 237, 237)); // in이 가리키는 부분 색깔 없애기

        // in의 인덱스를 다음 공간을 가리키도록 변경
        in = (in+1) % bufferSize;

        bufferOuterBox[in].setBackground(new Color(0,255,0)); // in이 새로 가리키게 된 부분 초록색으로 칠하기

        // 다른 스레드 깨우기
        notify();
    }

    // 소비자 스레드가 버퍼에 접근할 수 있게 해주는 함수
    // 버퍼에 있는 사칙연산을 consumingProblem 변수에 설정해줘서 소비자 스레드가 계산할 수 있게 함
    // 버퍼가 비어있으면 사칙연산이 들어올 때까지 기다림
    synchronized void consume(){
        // 버퍼가 비어있을 때
        if(in == out){
            try{
                // 그 공간을 파란색으로 칠하기
                bufferOuterBox[in].setBackground(new Color(0,0,255));
                // 버퍼에 사칙연산이 새로 들어올 때까지 대기
                wait();
            } catch(InterruptedException e){
                System.out.println();
            }
        }
        consumingProblem = buffer[out]; // 소비자 스레드에 전달해줄 사칙연산

        bufferOuterBox[out].setBackground(new Color(237, 237, 237)); // out이 가리키는 부분 색깔 없애기
        out = (out+1) % bufferSize; // out의 인덱스를 다음 공간을 가리키도록 변경
        bufferOuterBox[out].setBackground(new Color(255,0,0)); // out이 새로 가리키게 된 부분 빨간색으로 칠하기

        // 다른 스레드 깨우기
        notify();
    }
}
