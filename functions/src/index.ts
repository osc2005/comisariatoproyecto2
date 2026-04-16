import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { initializeApp } from "firebase-admin/app";
import { getFirestore, FieldValue } from "firebase-admin/firestore";

initializeApp();
const db = getFirestore();

export const gestionarStockCredito = onDocumentWritten(
  "creditos/{creditoId}",
  async (event) => {

    const antes = event.data?.before?.data();
    const despues = event.data?.after?.data();

    if (!despues) return;

    const productoId = despues.productoId as string;
    const cantidad = despues.cantidad as number;
    const estadoNuevo = despues.estado as string;
    const estadoAntes = antes?.estado as string | undefined;

    const productoRef = db.collection("productos").doc(productoId);

    // Caso 1: nueva reserva → restar stock
    if (!antes && estadoNuevo === "Pendiente") {
      await productoRef.update({ stock: FieldValue.increment(-cantidad) });
      return;
    }

    // Caso 2: admin rechaza → devolver stock
    if (estadoAntes === "Pendiente" && estadoNuevo === "Rechazado") {
      await productoRef.update({ stock: FieldValue.increment(cantidad) });
      return;
    }

    // Caso 3: cliente cancela → devolver stock
    if (estadoAntes === "Pendiente" && estadoNuevo === "Cancelado") {
      await productoRef.update({ stock: FieldValue.increment(cantidad) });
      return;
    }
  }
);