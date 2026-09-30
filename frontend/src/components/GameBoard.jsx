import GuessRow from './GuessRow'

export default function GameBoard({ guesses = [] }) {
  return (
    <div className="game-board" aria-label="Five guess game board">
      {Array.from({ length: 5 }, (_, index) => (
        <GuessRow key={guesses[index]?.guessNumber ?? `empty-${index}`} guess={guesses[index]} index={index} />
      ))}
    </div>
  )
}
