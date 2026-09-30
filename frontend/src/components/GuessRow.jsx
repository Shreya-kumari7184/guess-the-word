export default function GuessRow({ guess, index }) {
  const resultByPosition = guess?.results ?? []
  const letters = guess?.guess?.split('') ?? []

  return (
    <div className="guess-row" aria-label={guess ? `Guess ${guess.guessNumber}` : `Empty guess ${index + 1}`}>
      <span className="row-number">{guess?.guessNumber ?? index + 1}</span>
      {Array.from({ length: 5 }, (_, cellIndex) => {
        const result = resultByPosition[cellIndex]
        const letter = result?.letter ?? letters[cellIndex] ?? ''
        const color = result?.status?.toLowerCase() ?? ''
        return (
          <div key={cellIndex} className={`letter-cell ${color}`} aria-label={letter ? `${letter} ${color}` : 'Empty'}>
            {letter}
          </div>
        )
      })}
    </div>
  )
}
