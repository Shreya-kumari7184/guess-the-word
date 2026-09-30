export default function MessageModal({ open, title, message, onClose }) {
  if (!open) return null
  return (
    <div className="modal-backdrop" role="presentation">
      <section className="message-modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <div className="modal-check">✓</div>
        <h2 id="modal-title">{title}</h2>
        <p>{message}</p>
        <button className="button button-primary" onClick={onClose} type="button">OK</button>
      </section>
    </div>
  )
}
