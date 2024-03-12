package TinyCore.Core.Excute

import spinal.core._
import spinal.lib._
import TinyCore.Core.Decode._
import TinyCore.Core.Constant.Defines._
import Common.SpinalTools._

/* rebuild the ALU unit and contains all */

class ALUPlugin extends PrefixComponent{
  /* implement some arithmetic operation */

  import ALU._
  val io = new Bundle{
    val alu = in (ALU())
    val valid = in Bool()
    val op1 = in Bits(Xlen bits)
    val op2 = in Bits(Xlen bits)
    val res = out Bits(Xlen bits)
  }

  val bitsCal = io.alu.mux(
    AND -> (io.op1 & io.op2),
    OR -> (io.op1 | io.op2),
    XOR -> (io.op1 ^ io.op2),
    SLL -> (io.op1 |<< io.op2.asUInt), /* logic shift */
    SRL -> (io.op1 |>> io.op2.asUInt),
    SRA -> (io.op1.asSInt >> io.op2.asUInt).asBits, /* the arithmetic shift using */
    default -> io.op1
  )
  val lessU = io.alu === SLTU
  val less = Mux(lessU,io.op1.asUInt < io.op2.asUInt,io.op1.asSInt < io.op2.asSInt)

  val doSub = io.alu === SUB
  val addSub = Mux(doSub,io.op1.asSInt - io.op2.asSInt,io.op1.asSInt + io.op2.asSInt).asBits

  when(io.valid){
    io.res := io.alu.mux(
      (SLT,SLTU) -> less.asBits.resized,
      (ADD,SUB) -> addSub,
      default -> bitsCal
    )
  }.otherwise{
    io.res := B(0,Xlen bits)
  }
}